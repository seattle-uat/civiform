package views.errors;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.span;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.H1Tag;
import play.i18n.Messages;
import play.mvc.Http;
import play.twirl.api.Content;
import services.MessageKey;
import views.BaseHtmlView;
import views.HtmlBundle;
import views.LanguageSelector;
import views.applicant.ApplicantLayout;
import views.components.LinkElement;
import views.style.ApplicantStyles;
import views.style.ErrorStyles;

/**
 * Renders a page to handle internal server errors that will be shown to users instead of the
 * unthemed default Play page.
 */
public final class InternalServerError extends BaseHtmlView {

  private final ApplicantLayout layout;
  private final LanguageSelector languageSelector;
  private final String supportEmail;

  @Inject
  public InternalServerError(
      ApplicantLayout layout, Config configuration, LanguageSelector languageSelector) {
    this.layout = checkNotNull(layout);
    this.languageSelector = checkNotNull(languageSelector);
    this.supportEmail = checkNotNull(configuration).getString("support_email_address");
  }

  public Content render(Http.RequestHeader request, Messages messages, String exceptionId) {
    HtmlBundle bundle = addBodyFooter(request, messages, exceptionId);
    return layout.render(bundle);
  }

  private H1Tag h1Content(Messages messages) {
    return h1(span(messages.at(MessageKey.ERROR_INTERNAL_SERVER_TITLE.getKeyName())))
        .withClasses(ErrorStyles.H1_NOT_FOUND);
  }

  private DivTag descriptionContent(Messages messages, String exceptionId) {

    String emailLinkHref =
        String.format("mailto:%s?body=[CiviForm Error ID: %s]", supportEmail, exceptionId);

    ATag emailAction =
        new LinkElement()
            .setText(supportEmail)
            .setHref(emailLinkHref)
            .opensInNewTab()
            .asAnchorText()
            .withClasses(ApplicantStyles.LINK);

    String descriptionText =
        messages.at(MessageKey.ERROR_INTERNAL_SERVER_DESCRIPTION.getKeyName(), exceptionId);
    descriptionText = String.format(descriptionText, emailAction.render());

    return div(rawHtml(descriptionText)).withClasses(ErrorStyles.P_DESCRIPTION);
  }

  /** Page returned on 500 error */
  private DivTag mainContent(Messages messages, String exceptionId) {
    return div(h1Content(messages), descriptionContent(messages, exceptionId))
        .withClasses("text-center", "max-w-screen-sm", "w-5/6", "mx-auto");
  }

  private HtmlBundle addBodyFooter(
      Http.RequestHeader request, Messages messages, String exceptionId) {
    HtmlBundle bundle = layout.getBundle();
    String language = languageSelector.getPreferredLangage(request).code();
    bundle.setLanguage(language);
    bundle.addMainContent(mainContent(messages, exceptionId));

    return bundle;
  }
}
