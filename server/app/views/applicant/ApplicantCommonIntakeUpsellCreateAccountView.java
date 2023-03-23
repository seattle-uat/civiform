package views.applicant;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.*;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import java.util.Optional;
import models.Account;
import play.i18n.Messages;
import play.mvc.Http;
import play.twirl.api.Content;
import services.MessageKey;
import services.applicant.ApplicantService;
import views.BaseHtmlView;
import views.HtmlBundle;
import views.components.ToastMessage;
import views.style.ApplicantStyles;
import views.style.StyleUtils;

/** Renders a confirmation page after application submission, for the common intake form. */
public final class ApplicantCommonIntakeUpsellCreateAccountView extends BaseHtmlView {

  ApplicantLayout layout;

  @Inject
  public ApplicantCommonIntakeUpsellCreateAccountView(ApplicantLayout layout) {
    this.layout = checkNotNull(layout);
  }

  /** Renders a sign-up page with a baked-in redirect. */
  public Content render(
      Http.Request request,
      String redirectTo,
      Account account,
      Optional<String> applicantName,
      Long applicantId,
      Long programId,
      boolean isTrustedIntermediary,
      ImmutableList<ApplicantService.ApplicantProgramData> eligiblePrograms,
      Messages messages,
      Optional<ToastMessage> bannerMessage) {
    String title =
        isTrustedIntermediary
            ? messages.at(MessageKey.TITLE_COMMON_INTAKE_CONFIRMATION_TI.getKeyName())
            : messages.at(MessageKey.TITLE_COMMON_INTAKE_CONFIRMATION.getKeyName());
    HtmlBundle bundle = layout.getBundle().setTitle(title);
    // Don't show "create an account" upsell box to TIs, or anyone with an email address already.
    boolean shouldUpsell =
        Strings.isNullOrEmpty(account.getEmailAddress()) && account.getMemberOfGroup().isEmpty();

    ImmutableList<DomContent> actionButtons =
        shouldUpsell
            ? ImmutableList.of(
              // todo don't include this first button if there were eligible programs.
                redirectButton(
                        "go-back-and-edit",
                        messages.at(MessageKey.BUTTON_GO_BACK_AND_EDIT.getKeyName()),
                        controllers.applicant.routes.ApplicantProgramReviewController.review(
                                applicantId, programId)
                            .url())
                    .withClasses(ApplicantStyles.BUTTON_UPSELL_SECONDARY_ACTION),
                redirectButton(
                        "apply-to-programs",
                        messages.at(MessageKey.BUTTON_APPLY_TO_PROGRAMS.getKeyName()),
                        controllers.applicant.routes.ApplicantProgramsController.index(applicantId)
                            .url())
                    .withClasses(ApplicantStyles.BUTTON_UPSELL_SECONDARY_ACTION),
                redirectButton(
                        "sign-in",
                        // todo avaleske split this into create account and log in buttons
                        messages.at(MessageKey.LINK_CREATE_ACCOUNT_OR_SIGN_IN.getKeyName()),
                        controllers.routes.LoginController.applicantLogin(Optional.of(redirectTo))
                            .url())
                    .withClasses(ApplicantStyles.BUTTON_UPSELL_PRIMARY_ACTION))
            : ImmutableList.of(
                redirectButton(
                        "apply-to-programs",
                        messages.at(MessageKey.BUTTON_APPLY_TO_PROGRAMS.getKeyName()),
                        controllers.applicant.routes.ApplicantProgramsController.index(applicantId)
                            .url())
                    .withClasses(ApplicantStyles.BUTTON_UPSELL_PRIMARY_ACTION));

    var content =
        div()
            .withClasses(ApplicantStyles.PROGRAM_INFORMATION_BOX)
            .with(
                h1(title).withClasses("text-3xl", "text-black", "font-bold", "mb-4"),
                eligibleProgramsSection(eligiblePrograms, messages, isTrustedIntermediary)
                    .withClasses("mb-4"),
                section()
                    .condWith(
                        shouldUpsell,
                        h2(messages.at(MessageKey.TITLE_CREATE_AN_ACCOUNT.getKeyName()))
                            .withClasses("mb-4", "font-bold"),
                        div(messages.at(MessageKey.CONTENT_PLEASE_CREATE_ACCOUNT.getKeyName()))
                            .withClasses("mb-4"))
                    .with(
                        div()
                            .withClasses(
                                "flex",
                                "flex-col",
                                "gap-4",
                                StyleUtils.responsiveMedium("flex-row"))
                            // Empty div to push buttons to the right on desktop.
                            .with(div().withClasses("flex-grow"))
                            .with(actionButtons)));

    bannerMessage.ifPresent(bundle::addToastMessages);
    bundle.addMainStyles(ApplicantStyles.MAIN_PROGRAM_APPLICATION).addMainContent(content);
    return layout.renderWithNav(request, applicantName, messages, bundle);
  }

  private DivTag eligibleProgramsSection(
      ImmutableList<ApplicantService.ApplicantProgramData> eligiblePrograms,
      Messages messages,
      boolean isTrustedIntermediary) {
    var eligibleProgramsDiv = div();

    if (eligiblePrograms.isEmpty()) {
      return eligibleProgramsDiv.with(
          p(isTrustedIntermediary
                  ? messages.at(
                      MessageKey.CONTENT_COMMON_INTAKE_NO_MATCHING_PROGRAMS_TI.getKeyName())
                  : messages.at(MessageKey.CONTENT_COMMON_INTAKE_NO_MATCHING_PROGRAMS.getKeyName()))
              .withClasses("mb-4"),
          p(messages.at(
                  MessageKey.CONTENT_COMMON_INTAKE_NO_MATCHING_PROGRAMS_NEXT_STEP.getKeyName()))
              .withClasses("mb-4"));
    }

    return eligibleProgramsDiv.with(
        section(
            each(
                eligiblePrograms,
                ep ->
                    div(
                        h3(ep.program().localizedName().getOrDefault(messages.lang().toLocale()))
                            .withClasses("text-black", "font-bold"),
                        p(ep.program()
                                .localizedDescription()
                                .getOrDefault(messages.lang().toLocale()))
                            .withClasses("mb-4")))),
        section(
            p(isTrustedIntermediary
                    ? messages.at(MessageKey.CONTENT_COMMON_INTAKE_CONFIRMATION_TI.getKeyName())
                    : messages.at(MessageKey.CONTENT_COMMON_INTAKE_CONFIRMATION.getKeyName()))
                .withClasses("mb-4")));
  }
}
