package jobs;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Optional;

/**
 * Provides the means of looking up a {@link DurableJob} by its {@link DurableJobName}. This is
 * necessary because all {@link DurableJob}s are persisted by {@link models.PersistedDurableJob}
 * records. Also provides the means of retrieving a list of all recurring jobs with their associated
 * {@link RecurringJobExecutionTimeResolver}.
 */
public final class DurableJobRegistry {

  private final HashMap<String, RegisteredJob> registeredJobs = new HashMap<>();

  @AutoValue
  public abstract static class RegisteredJob {

    public static RegisteredJob create(
        DurableJobFactory durableJobFactory,
        DurableJobName jobName,
        Optional<RecurringJobExecutionTimeResolver> recurringJobExecutionTimeResolver) {
      return new AutoValue_DurableJobRegistry_RegisteredJob(
          durableJobFactory, jobName, recurringJobExecutionTimeResolver);
    }

    public abstract DurableJobFactory getFactory();

    public abstract DurableJobName getJobName();

    public abstract Optional<RecurringJobExecutionTimeResolver>
        getRecurringJobExecutionTimeResolver();

    public boolean isRecurring() {
      return this.getRecurringJobExecutionTimeResolver().isPresent();
    }
  }

  /** Registers a factory for a given job name. */
  public void register(DurableJobName jobName, DurableJobFactory durableJobFactory) {
    registeredJobs.put(
        jobName.getJobName(),
        RegisteredJob.create(
            durableJobFactory, jobName, /* recurringJobExecutionTimeResolver */ Optional.empty()));
  }

  /**
   * Registers a factory for a given job name along with a {@link RecurringJobExecutionTimeResolver}
   * that defines the future run times of the job.
   */
  public void register(
      DurableJobName jobName,
      DurableJobFactory durableJobFactory,
      RecurringJobExecutionTimeResolver recurringJobExecutionTimeResolver) {
    registeredJobs.put(
        jobName.getJobName(),
        RegisteredJob.create(
            durableJobFactory, jobName, Optional.of(recurringJobExecutionTimeResolver)));
  }

  /** Retrieves the job registered with the given name or throws {@link JobNotFoundException}. */
  public RegisteredJob get(DurableJobName jobName) throws JobNotFoundException {
    return Optional.ofNullable(registeredJobs.get(jobName.getJobName()))
        .orElseThrow(() -> new JobNotFoundException(jobName.getJobName()));
  }

  /** Returns all jobs registered with a {@link RecurringJobExecutionTimeResolver}. */
  public ImmutableSet<RegisteredJob> getRecurringJobs() {
    return registeredJobs.values().stream()
        .filter(RegisteredJob::isRecurring)
        .collect(ImmutableSet.toImmutableSet());
  }
}
