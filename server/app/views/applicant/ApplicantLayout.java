package views.applicant;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.img;
import static j2html.TagCreator.input;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.p;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;

import auth.CiviFormProfile;
import auth.ProfileUtils;
import auth.Role;
import com.typesafe.config.Config;
import controllers.routes;
import featureflags.FeatureFlags;
import io.jsonwebtoken.lang.Strings;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.H1Tag;
import j2html.tags.specialized.ImgTag;
import j2html.tags.specialized.InputTag;
import j2html.tags.specialized.NavTag;
import j2html.tags.specialized.SelectTag;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.i18n.Messages;
import play.mvc.Http;
import play.twirl.api.Content;
import services.DeploymentType;
import services.MessageKey;
import views.ApplicantUtils;
import views.BaseHtmlLayout;
import views.HtmlBundle;
import views.LanguageSelector;
import views.ViewUtils;
import views.components.LinkElement;
import views.components.buttons.Button;
import views.components.buttons.ButtonAction;
import views.components.buttons.ButtonStyle;
import views.components.buttons.ButtonStyles;
import views.html.helper.CSRF;
import views.style.ApplicantStyles;
import views.style.BaseStyles;
import views.style.StyleUtils;

/** Contains methods rendering common components used across applicant pages. */
public class ApplicantLayout extends BaseHtmlLayout {

  private static final Logger logger = LoggerFactory.getLogger(ApplicantLayout.class);

  private final BaseHtmlLayout layout;
  private final ProfileUtils profileUtils;
  public final LanguageSelector languageSelector;
  public final String supportEmail;
  private final Optional<String> maybeLogoUrl;
  private final String civicEntityFullName;
  private final String civicEntityShortName;

  @Inject
  public ApplicantLayout(
      BaseHtmlLayout layout,
      ViewUtils viewUtils,
      Config configuration,
      ProfileUtils profileUtils,
      LanguageSelector languageSelector,
      FeatureFlags featureFlags,
      DeploymentType deploymentType) {
    super(viewUtils, configuration, featureFlags, deploymentType);
    this.layout = layout;
    this.profileUtils = checkNotNull(profileUtils);
    this.languageSelector = checkNotNull(languageSelector);
    this.supportEmail = checkNotNull(configuration).getString("support_email_address");
    this.maybeLogoUrl =
        checkNotNull(configuration).hasPath("whitelabel.small_logo_url")
            ? Optional.of(configuration.getString("whitelabel.small_logo_url"))
            : Optional.empty();
    this.civicEntityFullName = configuration.getString("whitelabel.civic_entity_full_name");
    this.civicEntityShortName = configuration.getString("whitelabel.civic_entity_short_name");
  }

  @Override
  public Content render(HtmlBundle bundle) {
    bundle.addBodyStyles(ApplicantStyles.BODY);

    bundle.addFooterStyles("mt-24");

    Content rendered = super.render(bundle);
    if (!rendered.body().contains("<h1")) {
      logger.error("Page does not contain an <h1>, which is important for screen readers.");
    }
    if (Strings.countOccurrencesOf(rendered.body(), "<h1") > 1) {
      logger.error("Page contains more than one <h1>, which is detrimental to screen readers.");
    }
    return rendered;
  }

  // Same as renderWithNav, but defaults to no admin login link.
  public Content renderWithNav(
      Http.Request request, Optional<String> userName, Messages messages, HtmlBundle bundle) {
    return renderWithNav(request, userName, messages, bundle, /*includeAdminLogin=*/ false);
  }

  public Content renderWithNav(
      Http.Request request,
      Optional<String> userName,
      Messages messages,
      HtmlBundle bundle,
      boolean includeAdminLogin) {
    String language = languageSelector.getPreferredLangage(request).code();
    bundle.setLanguage(language);
    bundle.addHeaderContent(renderNavBar(request, userName, messages));

    ATag emailAction =
        new LinkElement()
            .setText(supportEmail)
            .setHref("mailto:" + supportEmail)
            .opensInNewTab()
            .asAnchorText()
            .withClasses(ApplicantStyles.LINK);
    ATag adminLoginAction =
        new LinkElement()
            .setText(messages.at(MessageKey.LINK_ADMIN_LOGIN.getKeyName()))
            .setHref(routes.LoginController.adminLogin().url())
            .asAnchorText()
            .withClasses(ApplicantStyles.LINK);

    DivTag footerDiv =
        div()
            .withClasses("flex", "flex-col")
            .with(
                div()
                    .with(
                        span(
                                text(
                                    messages.at(
                                        MessageKey.FOOTER_SUPPORT_LINK_DESCRIPTION.getKeyName())),
                                text(" "))
                            .withClasses("text-gray-600"),
                        emailAction)
                    .withClasses("mx-auto"),
                div()
                    .condWith(
                        includeAdminLogin,
                        span(
                                text(
                                    messages.at(
                                        MessageKey.CONTENT_ADMIN_FOOTER_PROMPT.getKeyName())),
                                text(" "),
                                adminLoginAction)
                            .withClasses("text-gray-600"))
                    .withClasses("mx-auto"));

    bundle.addFooterContent(footerDiv);

    return render(bundle);
  }

  private NavTag renderNavBar(Http.Request request, Optional<String> userName, Messages messages) {
    Optional<CiviFormProfile> profile = profileUtils.currentUserProfile(request);

    String displayUserName = ApplicantUtils.getApplicantName(userName, messages);
    return nav()
        .withClasses("bg-white", "border-b", "align-middle", "p-1", "grid", "grid-cols-3")
        .with(branding())
        .with(maybeRenderTiButton(profile, displayUserName))
        .with(
            div(getLanguageForm(request, profile, messages), authDisplaySection(userName, messages))
                .withClasses("justify-self-end", "flex", "flex-row"));
  }

  private ContainerTag<?> getLanguageForm(
      Http.Request request, Optional<CiviFormProfile> profile, Messages messages) {
    ContainerTag<?> languageFormDiv = div().withClasses("flex", "flex-col", "justify-center");

    if (profile.isPresent()) { // Show language switcher.
      long userId = profile.get().getApplicant().join().id;

      String applicantInfoUrl =
          controllers.applicant.routes.ApplicantInformationController.edit(userId).url();
      String updateLanguageAction =
          controllers.applicant.routes.ApplicantInformationController.update(userId).url();

      // Show language switcher if we're not on the applicant info page.
      boolean showLanguageSwitcher = !request.uri().equals(applicantInfoUrl);
      if (showLanguageSwitcher) {
        String csrfToken = CSRF.getToken(request.asScala()).value();
        InputTag csrfInput = input().isHidden().withValue(csrfToken).withName("csrfToken");
        InputTag redirectInput =
            input().isHidden().withValue(request.uri()).withName("redirectLink");
        String preferredLanguage = languageSelector.getPreferredLangage(request).code();
        SelectTag languageDropdown =
            languageSelector
                .renderDropdown(preferredLanguage)
                .attr("onchange", "this.form.submit()")
                .attr("aria-label", messages.at(MessageKey.LANGUAGE_LABEL_SR.getKeyName()));
        languageFormDiv =
            languageFormDiv.with(
                form()
                    .withAction(updateLanguageAction)
                    .withMethod(Http.HttpVerbs.POST)
                    .with(csrfInput)
                    .with(redirectInput)
                    .with(languageDropdown)
                    .with(
                        Button.builder()
                            .setText("")
                            .setStyle(ButtonStyle.NONE) // Doesn't matter since it's hidden.
                            .setButtonAction(ButtonAction.ofSubmit())
                            .setId("cf-update-lang")
                            .build()
                            .isHidden()));
      }
    }
    return languageFormDiv;
  }

  private ATag branding() {
    ImgTag cityImage;

    if (maybeLogoUrl.isPresent()) {
      cityImage = img().withSrc(maybeLogoUrl.get());
    } else {
      cityImage = this.layout.viewUtils.makeLocalImageTag("ChiefSeattle_Blue");
    }

    cityImage
        .withAlt(civicEntityFullName + " Logo")
        .attr("aria-hidden", "true")
        .withClasses("w-16", "py-1");

    return a().withHref(routes.HomeController.index().url())
        .withClasses("flex", "flex-row")
        .with(
            cityImage,
            div()
                .withId("brand-id")
                .withLang(Locale.ENGLISH.toLanguageTag())
                .withClasses(ApplicantStyles.CIVIFORM_LOGO)
                .with(p(b(civicEntityShortName), span(text(" CiviForm")))));
  }

  private DivTag maybeRenderTiButton(Optional<CiviFormProfile> profile, String userName) {
    if (profile.isPresent() && profile.get().getRoles().contains(Role.ROLE_TI.toString())) {
      String tiDashboardText = "View and Add Clients";
      String tiDashboardLink =
          controllers.ti.routes.TrustedIntermediaryController.dashboard(
                  /* nameQuery= */ Optional.empty(),
                  /* dateQuery= */ Optional.empty(),
                  /* page= */ Optional.of(1))
              .url();
      return div(
          a(tiDashboardText)
              .withId("ti-dashboard-link")
              .withHref(tiDashboardLink)
              .withClasses(
                  "opacity-75", StyleUtils.hover("opacity-100"), ButtonStyles.BUTTON_TI_DASHBOARD),
          div("(applying as: " + userName + ")")
              .withClasses("text-sm", "text-black", "text-center"));
    }
    return div();
  }

  /**
   * Shows authentication status and a button to take actions.
   *
   * <p>If the user is a guest, we show a "Log in" and a "Create an account" button. If they are
   * logged in, we show a "Logout" button.
   */
  private DivTag authDisplaySection(Optional<String> userName, Messages messages) {
    DivTag outsideDiv = div().withClasses("flex", "flex-col", "justify-center", "pr-4");

    if (ApplicantUtils.isGuest(userName, messages)) {
      String loggedInAsMessage = messages.at(MessageKey.GUEST_INDICATOR.getKeyName());
      String endSessionMessage = messages.at(MessageKey.END_SESSION.getKeyName());
      // Ending a guest session is equivalent to "logging out" the guest.
      String endSessionLink = org.pac4j.play.routes.LogoutController.logout().url();
      String logInMessage = messages.at(MessageKey.BUTTON_LOGIN.getKeyName());
      String logInLink = routes.LoginController.applicantLogin(Optional.empty()).url();
      String createAnAccountMessage = messages.at(MessageKey.BUTTON_CREATE_ACCOUNT.getKeyName());
      String createAnAccountLink = routes.LoginController.register().url();

      return outsideDiv.with(
          div(
              span(loggedInAsMessage).withClasses("text-sm"),
              text(" "),
              a(endSessionMessage)
                  .withHref(endSessionLink)
                  .withClasses(ApplicantStyles.LINK)
                  .withId("logout-button"),
              br(),
              a(logInMessage).withHref(logInLink).withClasses(ApplicantStyles.LINK),
              text("  |  "),
              a(createAnAccountMessage)
                  .withHref(createAnAccountLink)
                  .withClasses(ApplicantStyles.LINK)));
    } else {
      // TODO(#4626): make this a more robust check. userName should always be present here,
      // but a more foolproof solution would be better.
      String loggedInAsMessage =
          messages.at(MessageKey.USER_NAME.getKeyName(), userName.orElse("user"));
      String logoutLink = org.pac4j.play.routes.LogoutController.logout().url();
      return outsideDiv.with(
          div(loggedInAsMessage).withClasses("text-sm"),
          a(messages.at(MessageKey.BUTTON_LOGOUT.getKeyName()))
              .withId("logout-button")
              .withHref(logoutLink)
              .withClasses(ApplicantStyles.LINK));
    }
  }

  protected String renderPageTitleWithBlockProgress(
      String pageTitle, int blockIndex, int totalBlockCount) {
    // While applicant is filling out the application, include the block they are on as part of
    // their progress.
    blockIndex++;
    return String.format("%s — %d of %d", pageTitle, blockIndex, totalBlockCount);
  }

  /**
   * The progress indicator is a bit different while an application is being filled out vs for the
   * summary view.
   *
   * <p>While in progress, the current incomplete block is counted towards progress, but will not
   * show full progress while filling out the last block of the program.
   *
   * <p>For the summary view, there is no "current" block, and full progress can be shown.
   */
  protected DivTag renderProgramApplicationTitleAndProgressIndicator(
      String programTitle, int blockIndex, int totalBlockCount, boolean forSummary) {
    int percentComplete = getPercentComplete(blockIndex, totalBlockCount, forSummary);

    DivTag progressInner =
        div()
            .withClasses(
                BaseStyles.BG_SEATTLE_BLUE,
                "transition-all",
                "duration-300",
                "h-full",
                "block",
                "absolute",
                "left-0",
                "top-0",
                "w-1",
                "rounded-full")
            .withStyle("width:" + percentComplete + "%");
    DivTag progressIndicator =
        div(progressInner)
            .withId("progress-indicator")
            .withClasses(
                "border",
                BaseStyles.BORDER_SEATTLE_BLUE,
                "rounded-full",
                "font-semibold",
                "bg-white",
                "relative",
                "h-4",
                "mt-4");

    // While applicant is filling out the application, include the block they are on as part of
    // their progress.
    if (!forSummary) {
      blockIndex++;
    }

    String blockNumberText =
        forSummary ? "" : String.format("%d of %d", blockIndex, totalBlockCount);

    H1Tag programTitleContainer =
        h1().withClasses("flex")
            .with(span(programTitle).withClasses(ApplicantStyles.PROGRAM_TITLE))
            .condWith(
                !blockNumberText.isEmpty(),
                span().withClasses("flex-grow"),
                span(blockNumberText).withClasses("text-gray-500", "text-base", "text-right"));

    return div().with(programTitleContainer).with(progressIndicator);
  }

  /**
   * Returns whole number out of 100 representing the completion percent of this program.
   *
   * <p>See {@link #renderProgramApplicationTitleAndProgressIndicator(String, int, int, boolean)}
   * about why there's a difference between the percent complete for summary views, and for
   * non-summary views.
   */
  private int getPercentComplete(int blockIndex, int totalBlockCount, boolean forSummary) {
    if (totalBlockCount == 0) {
      return 100;
    }
    if (blockIndex == -1) {
      return 0;
    }

    // While in progress, add one to blockIndex for 1-based indexing, so that when applicant is on
    // first block, we show
    // some amount of progress; and add one to totalBlockCount so that when applicant is on the last
    // block, we show that they're
    // still in progress.
    // For summary views, we don't need to do any of the tricks, so we just use the actual total
    // block count and block index.
    double numerator = forSummary ? blockIndex : blockIndex + 1;
    double denominator = forSummary ? totalBlockCount : totalBlockCount + 1;

    return (int) (numerator / denominator * 100.0);
  }
}
