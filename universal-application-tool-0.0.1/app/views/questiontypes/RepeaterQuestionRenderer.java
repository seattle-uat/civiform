package views.questiontypes;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.div;

import com.google.common.collect.ImmutableList;
import j2html.tags.ContainerTag;
import j2html.tags.Tag;
import java.util.Optional;
import play.i18n.Messages;
import services.Path;
import services.applicant.question.ApplicantQuestion;
import services.applicant.question.RepeaterQuestion;
import views.BaseHtmlView;
import views.components.FieldWithLabel;
import views.style.ReferenceClasses;
import views.style.StyleUtils;
import views.style.Styles;

public class RepeaterQuestionRenderer extends BaseHtmlView implements ApplicantQuestionRenderer {

  private static final String ENUMERATOR_FIELDS_ID = "enumerator-fields";
  private static final String ADD_ELEMENT_BUTTON_ID = "enumerator-field-add-button";
  private static final String ENUMERATOR_FIELD_TEMPLATE_ID = "enumerator-field-template";
  private static final String PLACEHOLDER_ID = "enumerator-placeholder-text";
  private static final String ENUMERATOR_FIELD_CLASS = "enumerator-field";

  // TODO(#859): make this admin-configurable
  private static final String PLACEHOLDER = "Placeholder";

  public static final String ENUMERATOR_FIELD_CLASSES =
      StyleUtils.joinStyles(ENUMERATOR_FIELD_CLASS, Styles.FLEX, Styles.FLEX_ROW, Styles.MB_4);

  private final ApplicantQuestion question;

  public RepeaterQuestionRenderer(ApplicantQuestion question) {
    this.question = checkNotNull(question);
  }

  @Override
  public Tag render(Messages messages) {
    RepeaterQuestion enumeratorQuestion = question.createEnumeratorQuestion();
    ImmutableList<String> entityNames = enumeratorQuestion.getEntityNames();

    ContainerTag enumeratorFields = div().withId(ENUMERATOR_FIELDS_ID);
    int index;
    for (index = 0; index < entityNames.size(); index++) {
      enumeratorFields.with(existingEnumeratorField(Optional.of(entityNames.get(index)), index));
    }

    return div()
        .withClasses(Styles.MX_AUTO, Styles.W_MAX)
        .with(
            div()
                .withClasses(ReferenceClasses.APPLICANT_QUESTION_TEXT)
                .withText(question.getQuestionText()),
            div().withId(PLACEHOLDER_ID).withClass(Styles.HIDDEN).withText(PLACEHOLDER),
            div()
                .withClasses(
                    ReferenceClasses.APPLICANT_QUESTION_HELP_TEXT,
                    Styles.TEXT_BASE,
                    Styles.FONT_THIN,
                    Styles.MB_2)
                .withText(question.getQuestionHelpText()),
            enumeratorFields,
            button(ADD_ELEMENT_BUTTON_ID, messages.at("button.addEnumeratorEntity")))
        .withType("button");
  }

  public Tag existingEnumeratorField(Optional<String> existingOption, int index) {
    ContainerTag optionInput =
        FieldWithLabel.input()
            .setFieldName(question.getContextualizedPath().toString())
            .setValue(existingOption)
            .getContainer()
            .withClasses(Styles.FLEX, Styles.ML_2);
    Tag removeOptionBox =
        FieldWithLabel.checkbox()
            .setFieldName("delete[]")
            .setValue(String.valueOf(index))
            .getContainer();

    return div().withClasses(ENUMERATOR_FIELD_CLASSES).with(optionInput, removeOptionBox);
  }

  public static Tag newEnumeratorFieldTemplate(Path contextualizedPath) {
    ContainerTag optionInput =
        FieldWithLabel.input()
            .setFieldName(contextualizedPath.toString())
            .getContainer()
            .withClasses(Styles.FLEX, Styles.ML_2);
    Tag removeFieldButton = button("x").withType("button").withClasses(Styles.FLEX, Styles.ML_4);
    return div()
        .withId(ENUMERATOR_FIELD_TEMPLATE_ID)
        .withClasses(StyleUtils.joinStyles(ENUMERATOR_FIELD_CLASSES, Styles.HIDDEN))
        .with(optionInput, removeFieldButton);
  }
}
