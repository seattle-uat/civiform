package services;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import views.components.TextFormatter;

/**
 * Contains settings
 *
 * @param show Determines if the alert be displayed or not
 * @param title Alert title, if any
 * @param text Alert text
 * @param unescapedDescription true to use an unescaped description (th:utext). false otherwise.
 * @param alertType {@link AlertType}
 */
public record AlertSettings(
    Boolean show,
    Optional<String> title,
    String text,
    Boolean unescapedDescription,
    AlertType alertType,
    ImmutableList<String> additionalText) {
  public static AlertSettings empty() {
    return new AlertSettings(false, Optional.empty(), "", AlertType.NONE);
  }

  public AlertSettings(Boolean show, Optional<String> title, String text, AlertType alertType) {
    this(show, title, text, alertType, ImmutableList.of());
  }

  public AlertSettings(
      Boolean show,
      Optional<String> title,
      String text,
      AlertType alertType,
      ImmutableList<String> additionalText) {
    this(show, title, text, /* unescapedDescription= */ true, alertType, additionalText);
  }

  /** Sanitized HTML for the alert text that processes Markdown. */
  public String getFormattedAlertText(String text) {
    return TextFormatter.formatTextToSanitizedHTML(
        text, /* preserveEmptyLines= */ true, /* addRequiredIndicator= */ false);
  }
}
