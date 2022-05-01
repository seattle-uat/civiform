package services.applicant.question;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import services.MessageKey;
import services.Path;
import services.applicant.ValidationErrorMessage;
import services.question.types.QuestionType;

/**
 * Abstract implementation for the {@link Question} interface. Subclasses are expected to implement
 * the majority of the interface methods. Common logic is pulled up to this class for code sharing.
 */
abstract class QuestionImpl implements Question {
  protected final ApplicantQuestion applicantQuestion;

  public QuestionImpl(ApplicantQuestion applicantQuestion) {
    this.applicantQuestion = Preconditions.checkNotNull(applicantQuestion);
    if (!validQuestionTypes().contains(applicantQuestion.getType())) {
      throw new RuntimeException(
          String.format(
              "Question is not a question of the following types: [%s]: %s (type: %s)",
              Joiner.on(", ").join(validQuestionTypes().stream().toArray()),
              applicantQuestion.getQuestionDefinition().getQuestionPathSegment(),
              applicantQuestion.getQuestionDefinition().getQuestionType()));
    }
  }

  /**
   * The set of acceptable question types for the {@link ApplicantQuestion} provided in the
   * constructor. This is used for validation purposes.
   */
  protected abstract ImmutableSet<QuestionType> validQuestionTypes();

  @Override
  public final ImmutableMap<Path, ImmutableSet<ValidationErrorMessage>> getValidationErrors() {
    ImmutableMap<Path, String> failedUpdates =
        applicantQuestion.getApplicantData().getFailedUpdates();
    if (!isAnswered() && applicantQuestion.isOptional() && failedUpdates.isEmpty()) {
      return ImmutableMap.of();
    }
    // Why not just return the result of getValidationErrorsInternal()?
    // For ease of implementation, subclasses may build the error list by putting a field key
    // in the map along with a call to a validator method that may return an empty set of errors.
    // We remove keys with an empty set of errors here to help defend against downstream consumers
    // assumes that calling isEmpty on the map means that there are no errors.
    ImmutableMap<Path, ImmutableSet<ValidationErrorMessage>> result =
        ImmutableMap.<Path, ImmutableSet<ValidationErrorMessage>>builder()
            .putAll(Maps.filterEntries(getValidationErrorsInternal(), e -> !e.getValue().isEmpty()))
            .build();
    // We shouldn't have an empty error map if we failed to convert some of the input. If there
    // aren't
    // already errors, append a top-level error that the input couldn't be converted. In practice,
    // this shouldn't happen as long as each question type is properly accounting for bad input.
    if (result.isEmpty()
        && !failedUpdates.isEmpty()
        && getAllPaths().stream().anyMatch(failedUpdates::containsKey)) {
      result =
          ImmutableMap.of(
              applicantQuestion.getContextualizedPath(),
              ImmutableSet.of(ValidationErrorMessage.create(MessageKey.INVALID_INPUT)));
    }
    return result;
  }

  public final ImmutableMap<Path, String> getFailedUpdates() {
    return applicantQuestion.getApplicantData().getFailedUpdates();
  }

  /**
   * Question-type specific implementation of {@link Question.getValidationErrors}. Note that keys
   * with an empty set of errors will be filtered out by {@link Question.getValidationErrors} so
   * that calls to isEmpty on the getvalidationErrors result are sufficient to indicate if there any
   * errors.
   */
  protected abstract ImmutableMap<Path, ImmutableSet<ValidationErrorMessage>>
      getValidationErrorsInternal();

  /**
   * A question is considered answered if the applicant data has been set for any of the paths
   * associated with the question. If the applicant data does not contain the question's path, then
   * it will be considered unanswered.
   */
  @Override
  public boolean isAnswered() {
    return getAllPaths().stream().anyMatch(p -> applicantQuestion.getApplicantData().hasPath(p));
  }
}
