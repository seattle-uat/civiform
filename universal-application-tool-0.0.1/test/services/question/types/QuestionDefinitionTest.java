package services.question.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import services.CiviFormError;
import services.LocalizationUtils;
import services.Path;
import services.question.exceptions.TranslationNotFoundException;
import services.question.exceptions.UnsupportedQuestionTypeException;
import services.question.types.AddressQuestionDefinition.AddressValidationPredicates;
import services.question.types.TextQuestionDefinition.TextValidationPredicates;

@RunWith(JUnitParamsRunner.class)
public class QuestionDefinitionTest {
  private QuestionDefinitionBuilder builder;

  @Before
  public void setup() {
    builder =
        new QuestionDefinitionBuilder()
            .setName("my name")
            .setPath(Path.create("my.path.name"))
            .setDescription("description")
            .setQuestionType(QuestionType.TEXT)
            .setQuestionText(ImmutableMap.of(Locale.US, "question?"))
            .setQuestionHelpText(ImmutableMap.of(Locale.US, "help text"))
            .setValidationPredicates(TextValidationPredicates.builder().setMaxLength(128).build());
  }

  @Test
  public void testEquality_true() throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();
    assertThat(question.equals(question)).isTrue();
    QuestionDefinition sameQuestion = new QuestionDefinitionBuilder(question).build();
    assertThat(question.equals(sameQuestion)).isTrue();
  }

  @Test
  public void testEquality_nullReturnsFalse() throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();
    assertThat(question.equals(null)).isFalse();
  }

  @Test
  public void testEquality_differentPredicatesReturnsFalse()
      throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();

    QuestionDefinition differentQuestionPredicates =
        new QuestionDefinitionBuilder(question)
            .setValidationPredicates(TextValidationPredicates.builder().setMaxLength(1).build())
            .build();
    assertThat(question.equals(differentQuestionPredicates)).isFalse();
  }

  @Test
  public void testEquality_differentHelpTextReturnsFalse() throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();

    QuestionDefinition differentQuestionHelpText =
        new QuestionDefinitionBuilder(question)
            .setQuestionHelpText(ImmutableMap.of(Locale.US, "different help text"))
            .build();
    assertThat(question.equals(differentQuestionHelpText)).isFalse();
  }

  @Test
  public void testEquality_differentTextReturnsFalse() throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();

    QuestionDefinition differentQuestionText =
        new QuestionDefinitionBuilder(question)
            .setQuestionText(ImmutableMap.of(Locale.US, "question text?"))
            .build();
    assertThat(question.equals(differentQuestionText)).isFalse();
  }

  @Test
  public void testEquality_differentQuestionTypeReturnsFalse()
      throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();

    QuestionDefinition differentQuestionType =
        new QuestionDefinitionBuilder(question)
            .setQuestionType(QuestionType.ADDRESS)
            .setValidationPredicates(AddressValidationPredicates.create())
            .build();
    assertThat(question.equals(differentQuestionType)).isFalse();
  }

  @Test
  public void testEquality_differentIdReturnsFalse() throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();

    QuestionDefinition unpersistedQuestion = builder.clearId().build();
    assertThat(question.equals(unpersistedQuestion)).isFalse();

    QuestionDefinition differentId = builder.setId(456L).build();
    assertThat(question.equals(differentId)).isFalse();
  }

  @Test
  public void testEquality_differentNameReturnsFalse() throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();
    QuestionDefinition differentName = builder.setName("Different name").build();
    assertThat(question.equals(differentName)).isFalse();
  }

  @Test
  public void testEquality_differentDescriptionReturnsFalse()
      throws UnsupportedQuestionTypeException {
    QuestionDefinition question = builder.setId(123L).build();
    QuestionDefinition differentDescription =
        builder.setDescription("Different description").build();
    assertThat(question.equals(differentDescription)).isFalse();
  }

  @Test
  public void isRepeater_false() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "", Path.empty(), Optional.empty(), "", ImmutableMap.of(), ImmutableMap.of());

    assertThat(question.isRepeater()).isFalse();
  }

  @Test
  public void isRepeater_true() {
    QuestionDefinition question =
        new RepeaterQuestionDefinition(
            "", Path.empty(), Optional.empty(), "", ImmutableMap.of(), ImmutableMap.of());
    assertThat(question.isRepeater()).isTrue();
  }

  @Test
  public void isRepeated_false() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "", Path.empty(), Optional.empty(), "", ImmutableMap.of(), ImmutableMap.of());

    assertThat(question.isRepeated()).isFalse();
  }

  @Test
  public void isRepeated_true() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "", Path.empty(), Optional.of(123L), "", ImmutableMap.of(), ImmutableMap.of());
    assertThat(question.isRepeated()).isTrue();
  }

  @Test
  public void newQuestionHasCorrectFields() throws Exception {
    QuestionDefinition question =
        new QuestionDefinitionBuilder()
            .setQuestionType(QuestionType.TEXT)
            .setId(123L)
            .setName("my name")
            .setPath(Path.create("my.path.name"))
            .setDescription("description")
            .setQuestionText(ImmutableMap.of(Locale.US, "question?"))
            .setQuestionHelpText(ImmutableMap.of(Locale.US, "help text"))
            .setValidationPredicates(TextValidationPredicates.builder().setMinLength(0).build())
            .build();

    assertThat(question.getId()).isEqualTo(123L);
    assertThat(question.getName()).isEqualTo("my name");
    assertThat(question.getPath().path()).isEqualTo("my.path.name");
    assertThat(question.getDescription()).isEqualTo("description");
    assertThat(question.getQuestionText(Locale.US)).isEqualTo("question?");
    assertThat(question.getQuestionHelpText(Locale.US)).isEqualTo("help text");
    assertThat(question.getValidationPredicates())
        .isEqualTo(TextValidationPredicates.builder().setMinLength(0).build());
  }

  @Test
  public void getQuestionTextForUnknownLocale_throwsException() {
    String questionPath = "question.path";
    QuestionDefinition question =
        new TextQuestionDefinition(
            "",
            Path.create(questionPath),
            Optional.empty(),
            "",
            ImmutableMap.of(),
            ImmutableMap.of());

    Throwable thrown = catchThrowable(() -> question.getQuestionText(Locale.FRANCE));

    assertThat(thrown).isInstanceOf(TranslationNotFoundException.class);
    assertThat(thrown)
        .hasMessage(
            "Translation not found for Question at path: " + questionPath + "\n\tLocale: fr_FR");
  }

  @Test
  public void getQuestionHelpTextForUnknownLocale_throwsException() {
    String questionPath = "question.path";
    QuestionDefinition question =
        new TextQuestionDefinition(
            "",
            Path.create(questionPath),
            Optional.empty(),
            "",
            ImmutableMap.of(),
            ImmutableMap.of(Locale.US, "help text"));

    Throwable thrown = catchThrowable(() -> question.getQuestionHelpText(Locale.FRANCE));

    assertThat(thrown).isInstanceOf(TranslationNotFoundException.class);
    assertThat(thrown)
        .hasMessage(
            "Translation not found for Question at path: " + questionPath + "\n\tLocale: fr_FR");
  }

  @Test
  public void getEmptyHelpTextForUnknownLocale_succeeds() throws TranslationNotFoundException {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "", Path.empty(), Optional.empty(), "", ImmutableMap.of(), ImmutableMap.of());
    assertThat(question.getQuestionHelpText(Locale.FRANCE)).isEqualTo("");
  }

  @Test
  public void getQuestionTextOrDefault_returnsDefaultIfNotFound() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "",
            Path.empty(),
            Optional.empty(),
            "",
            ImmutableMap.of(LocalizationUtils.DEFAULT_LOCALE, "default"),
            ImmutableMap.of());

    assertThat(question.getQuestionTextOrDefault(Locale.forLanguageTag("und")))
        .isEqualTo("default");
  }

  @Test
  public void getQuestionHelpTextOrDefault_returnsDefaultIfNotFound() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "",
            Path.empty(),
            Optional.empty(),
            "",
            ImmutableMap.of(),
            ImmutableMap.of(LocalizationUtils.DEFAULT_LOCALE, "default"));

    assertThat(question.getQuestionHelpTextOrDefault(Locale.forLanguageTag("und")))
        .isEqualTo("default");
  }

  @Test
  public void newQuestionHasTypeText() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "", Path.empty(), Optional.empty(), "", ImmutableMap.of(), ImmutableMap.of());

    assertThat(question.getQuestionType()).isEqualTo(QuestionType.TEXT);
  }

  @Test
  public void validateWellFormedQuestion_returnsNoErrors() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "name",
            Path.create("path"),
            Optional.empty(),
            "description",
            ImmutableMap.of(Locale.US, "question?"),
            ImmutableMap.of());
    assertThat(question.validate()).isEmpty();
  }

  @Test
  public void validateBadQuestion_returnsErrors() {
    QuestionDefinition question =
        new TextQuestionDefinition(
            "", Path.empty(), Optional.empty(), "", ImmutableMap.of(), ImmutableMap.of());
    assertThat(question.validate())
        .containsOnly(
            CiviFormError.of("blank name"),
            CiviFormError.of("blank description"),
            CiviFormError.of("no question text"));
  }

  @Test
  public void getSupportedLocales_onlyReturnsFullySupportedLocales() {
    QuestionDefinition definition =
        new TextQuestionDefinition(
            "test",
            Path.empty(),
            Optional.empty(),
            "test",
            ImmutableMap.of(
                Locale.US,
                "question?",
                Locale.forLanguageTag("es-US"),
                "pregunta",
                Locale.FRANCE,
                "question"),
            ImmutableMap.of(
                Locale.US,
                "help",
                Locale.forLanguageTag("es-US"),
                "ayuda",
                Locale.GERMAN,
                "Hilfe"));

    assertThat(definition.getSupportedLocales())
        .containsExactly(Locale.US, Locale.forLanguageTag("es-US"));
  }
}
