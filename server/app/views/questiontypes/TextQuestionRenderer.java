package views.questiontypes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import j2html.tags.specialized.DivTag;
import services.Path;
import services.applicant.ValidationErrorMessage;
import services.applicant.question.ApplicantQuestion;
import services.applicant.question.TextQuestion;
import views.components.FieldWithLabel;

/** Renders a text question. */
public class TextQuestionRenderer extends ApplicantQuestionRendererImpl {

  public TextQuestionRenderer(ApplicantQuestion question) {
    super(question, InputFieldType.SINGLE);
  }

  @Override
  public String getReferenceClass() {
    return "cf-question-text";
  }

  @Override
  protected DivTag renderTag(
      ApplicantQuestionRendererParams params,
      ImmutableMap<Path, ImmutableSet<ValidationErrorMessage>> validationErrors,
      ImmutableList<String> ariaDescribedByIds,
      boolean hasErrors) {
    TextQuestion textQuestion = question.createTextQuestion();

    DivTag questionFormContent =
        FieldWithLabel.input()
            .setFieldName(textQuestion.getTextPath().toString())
            .setValue(textQuestion.getTextValue().orElse(""))
            .setFieldErrors(
                params.messages(),
                validationErrors.getOrDefault(textQuestion.getTextPath(), ImmutableSet.of()))
            .setAriaInvalid(hasErrors)
            .setAriaDescribedByIds(ariaDescribedByIds)
            .setScreenReaderText(question.getQuestionText())
            .getInputTag();

    return questionFormContent;
  }
}
