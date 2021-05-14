package services.applicant;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import services.Path;
import services.question.types.EnumeratorQuestionDefinition;

/** A repeated entity represents one of the applicant's answers to an enumerator question. */
@AutoValue
public abstract class RepeatedEntity {

  /** Create all the repeated entities associated with the enumerator question. */
  public static ImmutableList<RepeatedEntity> createRepeatedEntities(
      Optional<RepeatedEntity> parent,
      EnumeratorQuestionDefinition enumeratorQuestionDefinition,
      ApplicantData applicantData) {
    Path contextualizedEnumeratorPath =
        parent
            .map(RepeatedEntity::contextualizedPath)
            .orElse(ApplicantData.APPLICANT_PATH)
            .join(enumeratorQuestionDefinition.getQuestionPathSegment());
    ImmutableList<String> entityNames =
        applicantData.readRepeatedEntities(contextualizedEnumeratorPath);
    ImmutableList.Builder<RepeatedEntity> repeatedEntitiesBuilder = ImmutableList.builder();
    for (int i = 0; i < entityNames.size(); i++) {
      repeatedEntitiesBuilder.add(
          create(enumeratorQuestionDefinition, parent, entityNames.get(i), i));
    }
    return repeatedEntitiesBuilder.build();
  }

  private static RepeatedEntity create(
      EnumeratorQuestionDefinition enumeratorQuestionDefinition,
      Optional<RepeatedEntity> parent,
      String entityName,
      int index) {
    assert enumeratorQuestionDefinition.isEnumerator();
    return new AutoValue_RepeatedEntity(enumeratorQuestionDefinition, parent, entityName, index);
  }

  /**
   * The {@link services.question.types.QuestionType#ENUMERATOR} question definition associated with
   * this repeated entity.
   */
  public abstract EnumeratorQuestionDefinition enumeratorQuestionDefinition();

  /** If this is a nested repeated entity, this returns the immediate parent repeated entity. */
  public abstract Optional<RepeatedEntity> parent();

  /** The entity name provided by the applicant. */
  public abstract String entityName();

  /**
   * The positional index of this repeated entity with respect to the other repeated entities for
   * the applicant associated with this repeated entity's enumerator question.
   */
  public abstract int index();

  /** The contextualized path to the root of this repeated entity. */
  public Path contextualizedPath() {
    Path parentPath =
        parent().map(RepeatedEntity::contextualizedPath).orElse(ApplicantData.APPLICANT_PATH);
    return parentPath
        .join(enumeratorQuestionDefinition().getQuestionPathSegment())
        .atIndex(index());
  }
}
