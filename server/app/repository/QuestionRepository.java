package repository;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.annotation.TxIsolation;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;
import models.QuestionModel;
import models.QuestionTag;
import models.VersionModel;
import services.question.PrimaryApplicantInfoTag;
import services.question.exceptions.UnsupportedQuestionTypeException;
import services.question.types.QuestionDefinition;
import services.question.types.QuestionDefinitionBuilder;

/**
 * QuestionRepository performs complicated operations on {@link QuestionModel} that often involve
 * other EBean models or asynchronous handling.
 */
public final class QuestionRepository {
  private final QueryProfileLocationBuilder queryProfileLocationBuilder =
      new QueryProfileLocationBuilder("QuestionRepository");

  private final Database database;
  private final DatabaseExecutionContext executionContext;
  private final Provider<VersionRepository> versionRepositoryProvider;

  @Inject
  public QuestionRepository(
      DatabaseExecutionContext executionContext,
      Provider<VersionRepository> versionRepositoryProvider) {
    this.database = DB.getDefault();
    this.executionContext = checkNotNull(executionContext);
    this.versionRepositoryProvider = checkNotNull(versionRepositoryProvider);
  }

  public CompletionStage<Set<QuestionModel>> listQuestions() {
    return supplyAsync(
        () ->
            database
                .find(QuestionModel.class)
                .setLabel("QuestionModel.findSet")
                .setProfileLocation(queryProfileLocationBuilder.create("listQuestions"))
                .findSet(),
        executionContext);
  }

  public CompletionStage<Optional<QuestionModel>> lookupQuestion(long id) {
    return supplyAsync(
        () ->
            database
                .find(QuestionModel.class)
                .setLabel("QuestionModel.findById")
                .setProfileLocation(queryProfileLocationBuilder.create("lookupQuestion"))
                .setId(id)
                .findOneOrEmpty(),
        executionContext);
  }

  public CompletionStage<ImmutableList<QuestionDefinition>> lookupQuestionDefinitions(ImmutableList<Long> ids) {
    return supplyAsync(
      () ->
        database
          .find(QuestionModel.class)
          .setLabel("QuestionModel.findByIds")
          .setProfileLocation(queryProfileLocationBuilder.create("lookupQuestions"))
          .where()
          .in("id", ids)
          .findList()
          .stream()
          .map(QuestionModel::getQuestionDefinition)
          .collect(ImmutableList.toImmutableList()),
    executionContext);
  }

  public CompletionStage<ImmutableList<QuestionDefinition>> lookupQuestionDefinitionsWithPrimaryApplicantInfoTags(ImmutableList<Long> ids) {
    return supplyAsync(
      () ->
        database
          .find(QuestionModel.class)
          .setLabel("QuestionModel.findByIds")
          .setProfileLocation(queryProfileLocationBuilder.create("lookupQuestions"))
          .where()
          .and()
          .in("id", ids)
          .or()
          .arrayContains("question_tags", PrimaryApplicantInfoTag.APPLICANT_DOB.getQuestionTag().toString())
          .arrayContains("question_tags", PrimaryApplicantInfoTag.APPLICANT_EMAIL.getQuestionTag().toString())
          .arrayContains("question_tags", PrimaryApplicantInfoTag.APPLICANT_NAME.getQuestionTag().toString())
          .arrayContains("question_tags", PrimaryApplicantInfoTag.APPLICANT_PHONE.getQuestionTag().toString())
          .endOr()
          .endAnd()
          .findList()
          .stream()
          .map(QuestionModel::getQuestionDefinition)
          .collect(ImmutableList.toImmutableList()),
    executionContext);
  }

  /**
   * Find and update the DRAFT of the question with this name, if one already exists. Create a new
   * DRAFT if there isn't one.
   */
  public QuestionModel createOrUpdateDraft(QuestionDefinition definition) {
    VersionModel draftVersion = versionRepositoryProvider.get().getDraftVersionOrCreate();
    try (Transaction transaction =
        database.beginTransaction(TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE))) {
      Optional<QuestionModel> existingDraft =
          versionRepositoryProvider
              .get()
              .getQuestionByNameForVersion(definition.getName(), draftVersion);
      try {
        if (existingDraft.isPresent()) {
          QuestionModel updatedDraft =
              new QuestionModel(
                  new QuestionDefinitionBuilder(definition).setId(existingDraft.get().id).build());
          this.updateQuestionSync(updatedDraft);
          transaction.commit();
          return updatedDraft;
        }
        QuestionModel newDraftQuestion =
            new QuestionModel(new QuestionDefinitionBuilder(definition).setId(null).build());
        insertQuestionSync(newDraftQuestion);
        // Fetch the tags off the old question.
        QuestionModel oldQuestion = new QuestionModel(definition);
        oldQuestion.refresh();
        oldQuestion.getQuestionTags().forEach(newDraftQuestion::addTag);

        // TODO (#6058): Add export state to QuestionDefinition so that the QuestionDefinition
        // is the source of truth for the state of the tags and tags are set when creating
        // the Question, rather than having to copy the tags from the old question.
        //
        // Since we track the universal question state in the QuestionDefinition,
        // if we're adding UNIVERSAL to the question, it will add the tag when the Question
        // is created. But if we're trying to remove UNIVERSAL, then it's going to get
        // copied from the old question and we now need to remove it here.
        if (!definition.isUniversal()) {
          newDraftQuestion.removeTag(QuestionTag.UNIVERSAL);
        }

        // Same deal here.  We have to remove any QuestionTags for PrimaryApplicantInfoTags that are not present.
        PrimaryApplicantInfoTag.getAllTagsForQuestionType(definition.getQuestionType())
            .forEach(
                primaryApplicantInfoTag -> {
                  if (!definition.containsPrimaryApplicantInfoTag(primaryApplicantInfoTag)) {
                    newDraftQuestion.removeTag(primaryApplicantInfoTag.getQuestionTag());
                  }
                });

        newDraftQuestion.addVersion(draftVersion).save();
        draftVersion.refresh();

        // Update other questions that may reference the previous revision.
        if (definition.isEnumerator()) {
          transaction.setNestedUseSavepoint();
          updateAllRepeatedQuestions(newDraftQuestion.id, definition.getId());
        }

        // Update programs that reference the previous question. A bit round about but this will
        // update all questions
        // in the program to their latest version, including the one here.
        transaction.setNestedUseSavepoint();
        versionRepositoryProvider.get().updateProgramsThatReferenceQuestion(definition.getId());
        transaction.commit();
        return newDraftQuestion;
      } catch (UnsupportedQuestionTypeException e) {
        // This should not be able to happen since the provided question definition is inherently
        // valid.
        // Throw runtime exception so callers don't have to deal with it.
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Update DRAFT and ACTIVE questions that reference {@code oldEnumeratorId} to reference {@code
   * newEnumeratorId}.
   */
  public void updateAllRepeatedQuestions(long newEnumeratorId, long oldEnumeratorId) {
    VersionRepository versionRepository = versionRepositoryProvider.get();
    // TODO: This seems error prone as a question could be present as a DRAFT and ACTIVE.
    // Investigate further.
    Stream.concat(
            versionRepository
                .getQuestionsForVersion(versionRepository.getDraftVersionOrCreate())
                .stream(),
            versionRepository.getQuestionsForVersion(versionRepository.getActiveVersion()).stream())
        // Find questions that reference the old enumerator ID.
        .filter(
            question ->
                question
                    .getQuestionDefinition()
                    .getEnumeratorId()
                    .equals(Optional.of(oldEnumeratorId)))
        // Update to the new enumerator ID.
        .forEach(
            question -> {
              try {
                createOrUpdateDraft(
                    new QuestionDefinitionBuilder(question.getQuestionDefinition())
                        .setEnumeratorId(Optional.of(newEnumeratorId))
                        .build());
              } catch (UnsupportedQuestionTypeException e) {
                // All question definitions are looked up and should be valid.
                throw new RuntimeException(e);
              }
            });
  }

  /**
   * Maybe find a {@link QuestionModel} that conflicts with {@link QuestionDefinition}.
   *
   * <p>This is intended to be used for new question definitions, since updates will collide with
   * themselves and previous versions, and new versions of an old question will conflict with the
   * old question.
   *
   * <p>Questions collide if they share a {@link QuestionDefinition#getQuestionPathSegment()} and
   * {@link QuestionDefinition#getEnumeratorId()}.
   */
  public Optional<QuestionModel> findConflictingQuestion(QuestionDefinition newQuestionDefinition) {
    ConflictDetector conflictDetector =
        new ConflictDetector(
            newQuestionDefinition.getEnumeratorId(),
            newQuestionDefinition.getQuestionPathSegment(),
            newQuestionDefinition.getName());
    database
        .find(QuestionModel.class)
        .setLabel("QuestionModel.findConflict")
        .setProfileLocation(queryProfileLocationBuilder.create("findConflictingQuestion"))
        .findEachWhile(question -> !conflictDetector.hasConflict(question));
    return conflictDetector.getConflictedQuestion();
  }

  /** Get the questions with the specified tag which are in the active version. */
  public ImmutableList<QuestionDefinition> getAllQuestionsForTag(QuestionTag tag) {
    VersionModel active = versionRepositoryProvider.get().getActiveVersion();
    ImmutableSet<Long> activeQuestionIds =
        versionRepositoryProvider.get().getQuestionsForVersion(active).stream()
            .map(q -> q.id)
            .collect(ImmutableSet.toImmutableSet());
    return database
        .find(QuestionModel.class)
        .setLabel("QuestionModel.findList")
        .setProfileLocation(queryProfileLocationBuilder.create("getAllQuestionsForTag"))
        .where()
        .arrayContains("question_tags", tag)
        .findList()
        .stream()
        .filter(question -> activeQuestionIds.contains(question.id))
        .sorted(Comparator.comparing(question -> question.getQuestionDefinition().getName()))
        .map(QuestionModel::getQuestionDefinition)
        .collect(ImmutableList.toImmutableList());
  }

  public ImmutableMap<String, QuestionDefinition> getExistingQuestions(
      ImmutableSet<String> questionNames) {
    // We need to retrieve the latest id for each question since multiple versions of a question
    // with the same name can exist. We achieve this by a custom merge function that chooses the
    // value with the greater ID.
    return database
        .find(QuestionModel.class)
        .setLabel("QuestionModel.findList")
        .setProfileLocation(queryProfileLocationBuilder.create("getExistingQuestions"))
        .where()
        .in("name", questionNames)
        .orderBy()
        .asc("id")
        .findList()
        .stream()
        .map(QuestionModel::getQuestionDefinition)
        .collect(
            ImmutableMap.toImmutableMap(
                QuestionDefinition::getName,
                q -> q,
                (q1, q2) -> q1.getId() > q2.getId() ? q1 : q2));
  }

  private static final class ConflictDetector {
    private Optional<QuestionModel> conflictedQuestion = Optional.empty();
    private final Optional<Long> enumeratorId;
    private final String questionPathSegment;
    private final String questionName;

    private ConflictDetector(
        Optional<Long> enumeratorId, String questionPathSegment, String questionName) {
      this.enumeratorId = checkNotNull(enumeratorId);
      this.questionPathSegment = checkNotNull(questionPathSegment);
      this.questionName = checkNotNull(questionName);
    }

    private Optional<QuestionModel> getConflictedQuestion() {
      return conflictedQuestion;
    }

    private boolean hasConflict(QuestionModel question) {
      QuestionDefinition definition = question.getQuestionDefinition();
      boolean isSameName = definition.getName().equals(questionName);
      boolean isSameEnumId = definition.getEnumeratorId().equals(enumeratorId);
      boolean isSamePath = definition.getQuestionPathSegment().equals(questionPathSegment);
      if (isSameName || (isSameEnumId && isSamePath)) {
        conflictedQuestion = Optional.of(question);
        return true;
      }
      return false;
    }
  }

  public CompletionStage<QuestionModel> insertQuestion(QuestionModel question) {
    return supplyAsync(
        () -> {
          database.insert(question);
          return question;
        },
        executionContext);
  }

  public QuestionModel insertQuestionSync(QuestionModel question) {
    database.insert(question);
    return question;
  }

  public CompletionStage<QuestionModel> updateQuestion(QuestionModel question) {
    return supplyAsync(
        () -> {
          database.update(question);
          return question;
        },
        executionContext);
  }

  public QuestionModel updateQuestionSync(QuestionModel question) {
    database.update(question);
    return question;
  }
}
