package forms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Optional;
import models.LifecycleStage;
import org.junit.Test;
import services.Path;
import services.question.types.QuestionDefinition;
import services.question.types.QuestionDefinitionBuilder;
import services.question.types.TextQuestionDefinition;

public class TextQuestionFormTest {

  @Test
  public void getBuilder_returnsCompleteBuilder() throws Exception {
    Path path = Path.create("my.question.path.name");

    TextQuestionForm form = new TextQuestionForm();
    form.setQuestionName("name");
    form.setQuestionDescription("description");
    form.setQuestionText("What is the question text?");
    form.setQuestionHelpText("");
    form.setMinLength("4");
    form.setMaxLength("6");
    QuestionDefinitionBuilder builder = form.getBuilder(path);

    builder.setVersion(1L);
    builder.setLifecycleStage(LifecycleStage.ACTIVE);

    TextQuestionDefinition expected =
        new TextQuestionDefinition(
            1L,
            "name",
            path,
            Optional.empty(),
            "description",
            LifecycleStage.ACTIVE,
            ImmutableMap.of(Locale.US, "What is the question text?"),
            ImmutableMap.of(),
            TextQuestionDefinition.TextValidationPredicates.create(4, 6));

    QuestionDefinition actual = builder.build();

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getBuilder_withQdConstructor_returnsCompleteBuilder() throws Exception {
    Path path = Path.create("my.question.path.name");

    TextQuestionDefinition originalQd =
        new TextQuestionDefinition(
            1L,
            "name",
            path,
            Optional.empty(),
            "description",
            LifecycleStage.ACTIVE,
            ImmutableMap.of(Locale.US, "What is the question text?"),
            ImmutableMap.of(),
            TextQuestionDefinition.TextValidationPredicates.create(4, 6));

    TextQuestionForm form = new TextQuestionForm(originalQd);
    QuestionDefinitionBuilder builder = form.getBuilder(path);

    builder.setVersion(1L);
    builder.setLifecycleStage(LifecycleStage.ACTIVE);

    QuestionDefinition actual = builder.build();

    assertThat(actual).isEqualTo(originalQd);
  }

  @Test
  public void getBuilder_emptyStringMinMax_noPredicateSet() throws Exception {
    Path path = Path.create("my.question.path.name");

    TextQuestionForm form = new TextQuestionForm();
    form.setQuestionName("name");
    form.setQuestionDescription("description");
    form.setQuestionText("What is the question text?");
    form.setQuestionHelpText("");
    form.setMinLength("");
    form.setMaxLength("");
    QuestionDefinitionBuilder builder = form.getBuilder(path);

    builder.setVersion(1L);
    builder.setLifecycleStage(LifecycleStage.ACTIVE);

    TextQuestionDefinition expected =
        new TextQuestionDefinition(
            1L,
            "name",
            path,
            Optional.empty(),
            "description",
            LifecycleStage.ACTIVE,
            ImmutableMap.of(Locale.US, "What is the question text?"),
            ImmutableMap.of(),
            TextQuestionDefinition.TextValidationPredicates.create());

    QuestionDefinition actual = builder.build();

    assertThat(actual).isEqualTo(expected);
  }
}
