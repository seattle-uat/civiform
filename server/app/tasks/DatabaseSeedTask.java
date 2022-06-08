package tasks;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.SerializableConflictException;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.annotation.TxIsolation;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.NonUniqueResultException;
import javax.persistence.RollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.i18n.Lang;
import repository.VersionRepository;
import services.CiviFormError;
import services.ErrorAnd;
import services.LocalizedStrings;
import services.question.QuestionService;
import services.question.types.DateQuestionDefinition;
import services.question.types.NameQuestionDefinition;
import services.question.types.QuestionDefinition;

/**
 * Task for seeding the database. All of its work is done in the constructor, which is its only
 * public method.
 *
 * <p>Logic for seeding different resources should be factored into separate methods for clarity.
 *
 * <p>To avoid attempting to insert duplicate resources it uses a thread-local database transaction
 * with a transaction scope of SERIALIZABLE. If the transaction fails it retries up to {@code
 * MAX_RETRIES} times.
 */
public final class DatabaseSeedTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSeedTask.class);
  private static final int MAX_RETRIES = 10;
  private static final ImmutableList<QuestionDefinition> CANONICAL_QUESTIONS =
      ImmutableList.of(
          new NameQuestionDefinition(
              /* name= */ "Applicant Name",
              /* enumeratorId= */ Optional.empty(),
              /* description= */ "The applicant's name",
              /* questionText= */ LocalizedStrings.of(
                  ImmutableMap.of(
                      Lang.forCode("am").toLocale(), "ስም (የመጀመሪያ ስም እና የመጨረሻ ስም አህጽሮት ይሆናል)",
                      Lang.forCode("ko").toLocale(), "성함 (이름 및 성의 경우 이니셜도 괜찮음)",
                      Lang.forCode("so").toLocale(), "Magaca (magaca koowaad iyo kan dambe okay)",
                      Lang.forCode("tl").toLocale(),
                          "Pangalan (unang pangalan at ang unang titik ng apilyedo ay okay)",
                      Lang.forCode("vi").toLocale(), "Tên (tên và họ viết tắt đều được)",
                      Lang.forCode("en-US").toLocale(), "Please enter your first and last name",
                      Lang.forCode("es-US").toLocale(),
                          "Nombre (nombre y la inicial del apellido está bien)",
                      Lang.forCode("zh-TW").toLocale(), "姓名（名字和姓氏第一個字母便可）")),
              /* questionHelpText= */ LocalizedStrings.empty()),
          new DateQuestionDefinition(
              /* name= */ "Applicant Date of Birth",
              /* enumeratorId= */ Optional.empty(),
              /* description= */ "Applicant's date of birth",
              /* questionText= */ LocalizedStrings.of(
                  Lang.forCode("en-US").toLocale(),
                  "Please enter your date of birth in the format mm/dd/yyyy"),
              /* questionHelpText= */ LocalizedStrings.empty()));

  private final Provider<QuestionService> questionServiceProvider;
  private final Provider<VersionRepository> versionRepository;
  private final Database database;

  @Inject
  public DatabaseSeedTask(
      Provider<QuestionService> questionService, Provider<VersionRepository> versionRepository) {
    this.questionServiceProvider = checkNotNull(questionService);
    this.versionRepository = checkNotNull(versionRepository);
    this.database = DB.getDefault();

    this.run();
  }

  private void run() {
    if (isMissingCanonicalQuestions()) {
      // Ensure a draft version exists to avoid transaction collisions.
      versionRepository.get().getDraftVersion();
      inSerializableTransaction(this::seedCanonicalQuestions, 1);
    }
  }

  /**
   * Ensures that questions with names matching those in {@code CANONICAL_QUESTIONS} are present in
   * the database, inserting the definitions in {@code CANONICAL_QUESTIONS} if any aren't found.
   */
  private void seedCanonicalQuestions() {
    var questionService = questionServiceProvider.get();
    ImmutableSet<String> existingQuestionNames = questionService.getQuestionNames();

    for (QuestionDefinition questionDefinition : CANONICAL_QUESTIONS) {
      if (existingQuestionNames.contains(questionDefinition.getName())) {
        LOGGER.info(
            "Canonical question \"%s\" exists at server start", questionDefinition.getName());
        continue;
      }

      ErrorAnd<QuestionDefinition, CiviFormError> result =
          questionService.create(questionDefinition);

      if (result.isError()) {
        String errorMessages =
            result.getErrors().stream()
                .map(CiviFormError::message)
                .collect(Collectors.joining(", "));

        LOGGER.error(
            String.format(
                "Unable to create canonical question \"%s\" due to %s",
                questionDefinition.getName(), errorMessages));
      } else {
        LOGGER.info("Created canonical question \"%s\"", questionDefinition.getName());
      }
    }
  }

  private void inSerializableTransaction(Runnable fn, int tryCount) {
    Transaction transaction =
        database.beginTransaction(TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE));

    try {
      fn.run();
      transaction.commit();
    } catch (NonUniqueResultException | SerializableConflictException | RollbackException e) {
      LOGGER.warn("Database seed transaction failed: %s", e);

      if (tryCount > MAX_RETRIES) {
        throw new RuntimeException(e);
      }

      transaction.end();
      inSerializableTransaction(fn, ++tryCount);
    } finally {
      if (transaction.isActive()) {
        transaction.end();
      }
    }
  }

  private boolean isMissingCanonicalQuestions() {
    var questionService = questionServiceProvider.get();
    ImmutableSet<String> existingQuestionNames = questionService.getQuestionNames();

    return !existingQuestionNames.containsAll(
        CANONICAL_QUESTIONS.stream()
            .map(QuestionDefinition::getName)
            .collect(ImmutableList.toImmutableList()));
  }
}
