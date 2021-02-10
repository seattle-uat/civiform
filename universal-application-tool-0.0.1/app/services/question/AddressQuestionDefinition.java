package services.question;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;

public class AddressQuestionDefinition extends QuestionDefinition {

  public AddressQuestionDefinition(
      String id,
      String version,
      String name,
      String path,
      String description,
      ImmutableMap<Locale, String> questionText,
      ImmutableMap<Locale, String> questionHelpText) {
    super(id, version, name, path, description, questionText, questionHelpText);
  }

  @Override
  public QuestionType getQuestionType() {
    return QuestionType.ADDRESS;
  }

  @Override
  public ImmutableMap<String, ScalarType> getScalars() {
    return ImmutableMap.of(
        "street",
        ScalarType.STRING,
        "city",
        ScalarType.STRING,
        "state",
        ScalarType.STRING,
        "zip",
        ScalarType.STRING);
  }
}
