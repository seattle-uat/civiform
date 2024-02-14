package durablejobs;

import annotations.BindingAnnotations;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import models.PersistedDurableJobModel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import repository.PersistedDurableJobRepository;
import services.cloud.aws.SimpleEmail;

/**
 * Executes {@link DurableJob}s when their time has come.
 *
 * <p>DurableJobRunner is a singleton and its {@code runJobs} method is {@code synchronized} to
 * prevent overlapping executions within the same server at the same time.
 */
@Singleton
public final class DurableJobRunner {

  // private static final Logger LOGGER = LoggerFactory.getLogger(DurableJobRunner.class);

  private final String hostName;
  private final Database database = DB.getDefault();
  private final DurableJobExecutionContext durableJobExecutionContext;
  private final DurableJobRegistry durableJobRegistry;
  private final String itEmailAddress;
  private final int jobTimeoutMinutes;
  private final PersistedDurableJobRepository persistedDurableJobRepository;
  private final Provider<LocalDateTime> nowProvider;
  private final SimpleEmail simpleEmail;
  private final ZoneOffset zoneOffset;
  private final int runnerLifespanSeconds;

  @Inject
  public DurableJobRunner(
      Config config,
      DurableJobExecutionContext durableJobExecutionContext,
      DurableJobRegistry durableJobRegistry,
      PersistedDurableJobRepository persistedDurableJobRepository,
      @BindingAnnotations.Now Provider<LocalDateTime> nowProvider,
      SimpleEmail simpleEmail,
      ZoneId zoneId) {
    this.hostName =
        config.getString("base_url").replace("https", "").replace("http", "").replace("://", "");
    this.durableJobExecutionContext = Preconditions.checkNotNull(durableJobExecutionContext);
    this.durableJobRegistry = Preconditions.checkNotNull(durableJobRegistry);
    this.itEmailAddress =
        config.getString("it_email_address").isBlank()
            ? config.getString("support_email_address")
            : config.getString("it_email_address");
    this.jobTimeoutMinutes = config.getInt("durable_jobs.job_timeout_minutes");
    this.persistedDurableJobRepository = Preconditions.checkNotNull(persistedDurableJobRepository);
    this.runnerLifespanSeconds = config.getInt("durable_jobs.poll_interval_seconds");
    this.simpleEmail = Preconditions.checkNotNull(simpleEmail);
    this.nowProvider = Preconditions.checkNotNull(nowProvider);
    this.zoneOffset = zoneId.getRules().getOffset(nowProvider.get());
  }

  /**
   * Queries for durable jobs that are ready to run and executes them.
   *
   * <p>Continues executing jobs as long as there are jobs to execute and it does not exceed the
   * time specified by "durable_jobs.poll_interval_seconds". This is to prevent runners attempting
   * to run at the same time in the same server.
   *
   * <p>{@code synchronized} to avoid overlapping executions within the same server.
   */
  public synchronized void runJobs() {
    System.err.println(
        "JobRunner_Start thread ID="
            + Thread.currentThread().getId()
            + "  from "
            + Arrays.toString(Thread.currentThread().getStackTrace()));

    LocalDateTime stopTime = resolveStopTime();
    Transaction transaction = database.beginTransaction();
    Optional<PersistedDurableJobModel> maybeJobToRun =
        persistedDurableJobRepository.getJobForExecution();

    while (maybeJobToRun.isPresent() && nowProvider.get().isBefore(stopTime)) {
      System.err.println(
          "jobToRun="
              + maybeJobToRun.get().id
              + "  nowTime="
              + nowProvider.get()
              + "  stopTime="
              + stopTime);
      PersistedDurableJobModel jobToRun = maybeJobToRun.get();
      runJob(jobToRun);
      notifyUponFinalFailure(jobToRun);
      transaction.commit();

      transaction = database.beginTransaction();
      maybeJobToRun = persistedDurableJobRepository.getJobForExecution();
    }
    transaction.close();

    System.err.println("JobRunner_Stop thread ID=" + Thread.currentThread().getId());
  }

  private void notifyUponFinalFailure(PersistedDurableJobModel job) {
    if (!job.hasFailedWithNoRemainingAttempts()) {
      return;
    }

    String subject = String.format("ERROR: CiviForm Durable job failure on %s", hostName);
    StringBuilder contents = new StringBuilder("A durable job has failed repeatedly on ");
    contents.append(hostName);
    contents.append("\n\n");

    contents.append(
        "This needs to be investigated by IT staff or the CiviForm core team"
            + " (civiform-technical@googlegroups.com).\n\n");
    contents.append(
        String.format("Error report for: job_name=\"%s\", job_ID=%d\n", job.getJobName(), job.id));
    contents.append(job.getErrorMessage().orElse("Job is missing error messages."));

    simpleEmail.send(itEmailAddress, subject, contents.toString());
  }

  private void runJob(PersistedDurableJobModel persistedDurableJob) {
    LocalDateTime startTime = nowProvider.get();
    System.err.println(
        String.format(
            "JobRunner_ExecutingJob thread_ID={%s}, job_name=\"{%s}\", job_ID={%s},"
                + " start_time={%s}",
            Thread.currentThread().getId(),
            persistedDurableJob.getJobName(),
            persistedDurableJob.id,
            startTime));

    try {
      System.err.println("Before decrement attempts");
      persistedDurableJob.decrementRemainingAttempts().save();

      // Run the job in a separate thread and block until it completes, fails, or times out.
      System.err.println("Before runJobWithTimeout");
      runJobWithTimeout(
          durableJobRegistry
              .get(DurableJobName.valueOf(persistedDurableJob.getJobName()))
              .getFactory()
              .create(persistedDurableJob));

      System.err.println("Before setSuccessTime");
      persistedDurableJob.setSuccessTime(nowProvider.get().toInstant(zoneOffset)).save();

      System.err.println(
          String.format(
              "JobRunner_JobSucceeded job_name=\"{%s}\", job_ID={%s}, duration_s={%s}",
              persistedDurableJob.getJobName(),
              persistedDurableJob.id,
              getJobDurationInSeconds(startTime)));
    } catch (JobNotFoundException
        | IllegalArgumentException
        | CancellationException
        | InterruptedException e) {
      String msg =
          String.format(
              "JobRunner_JobFailed %s job_name=\"%s\", job_ID=%d, attempts_remaining=%d,"
                  + " duration_s=%f",
              e.getClass().getSimpleName(),
              persistedDurableJob.getJobName(),
              persistedDurableJob.id,
              persistedDurableJob.getRemainingAttempts(),
              getJobDurationInSeconds(startTime));
      System.err.println(msg);
      persistedDurableJob.appendErrorMessage(msg).save();
    } catch (TimeoutException e) {
      String msg =
          String.format(
              "JobRunner_JobTimeout job_name=\"%s\", job_ID=%d, attempts_remaining=%d,"
                  + " duration_s=%f",
              persistedDurableJob.getJobName(),
              persistedDurableJob.id,
              persistedDurableJob.getRemainingAttempts(),
              getJobDurationInSeconds(startTime));
      System.err.println(msg);
      persistedDurableJob.appendErrorMessage(msg).save();
    } catch (ExecutionException e) {
      String msg =
          String.format(
              "JobRunner_JobFailed ExecutionException job_name=\"%s\", job_ID=%d,"
                  + " attempts_remaining=%d, duration_s=%f, error_message=%s, trace=%s",
              persistedDurableJob.getJobName(),
              persistedDurableJob.id,
              persistedDurableJob.getRemainingAttempts(),
              getJobDurationInSeconds(startTime),
              e.getMessage(),
              ExceptionUtils.getStackTrace(e));
      System.err.println(msg);
      persistedDurableJob.appendErrorMessage(msg).save();
    } catch (Throwable e) {
      String msg =
          String.format(
              "JobRunner_JobFailed UNKNOWN THROWABLE job_name=\"%s\", job_ID=%d,"
                  + " attempts_remaining=%d, duration_s=%f, error_message=%s, trace=%s",
              persistedDurableJob.getJobName(),
              persistedDurableJob.id,
              persistedDurableJob.getRemainingAttempts(),
              getJobDurationInSeconds(startTime),
              e.getMessage(),
              ExceptionUtils.getStackTrace(e));
      System.err.println(msg);
    }
  }

  private LocalDateTime resolveStopTime() {
    System.err.println("lifespan seconds=" + runnerLifespanSeconds);
    // We set poll interval to 0 in test
    if (runnerLifespanSeconds == 0) {
      // Run for no more than 5 seconds
      return nowProvider.get().plus(5000, ChronoUnit.MILLIS);
    }

    return nowProvider.get().plus(runnerLifespanSeconds, ChronoUnit.SECONDS);
  }

  private synchronized void runJobWithTimeout(DurableJob jobToRun)
      throws ExecutionException, InterruptedException, TimeoutException {
    CompletableFuture<Void> future =
        CompletableFuture.runAsync(() -> jobToRun.run(), durableJobExecutionContext.current());

    System.err.println("job timeout=" + jobTimeoutMinutes);
    // We set the job timeout to 0 in test
    if (jobTimeoutMinutes == 0) {
      // Timeout test jobs after 2500ms
      future.get(2500, TimeUnit.MILLISECONDS);
      return;
    }

    future.get(jobTimeoutMinutes, TimeUnit.MINUTES);
  }

  private double getJobDurationInSeconds(LocalDateTime startTime) {
    return ((double) ChronoUnit.MILLIS.between(startTime, nowProvider.get())) / 1000;
  }
}
