package views.applicant;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.attributes.Attr.HREF;

import com.google.common.collect.ImmutableList;
import controllers.applicant.routes;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;
import play.i18n.Messages;
import play.mvc.Http;
import play.twirl.api.Content;
import services.MessageKey;
import services.program.ProgramDefinition;
import views.BaseHtmlView;
import views.HtmlBundle;
import views.components.LinkElement;
import views.components.ToastMessage;
import views.style.ApplicantStyles;
import views.style.BaseStyles;
import views.style.ReferenceClasses;
import views.style.StyleUtils;
import views.style.Styles;

/** Returns a list of programs that an applicant can browse, with buttons for applying. */
public class ProgramIndexView extends BaseHtmlView {

  private final ApplicantLayout layout;

  @Inject
  public ProgramIndexView(ApplicantLayout layout) {
    this.layout = checkNotNull(layout);
  }

  /**
   * For each program in the list, render the program information along with an "Apply" button that
   * redirects the user to that program's application.
   *
   * @param messages the localized {@link Messages} for the current applicant
   * @param applicantId the ID of the current applicant
   * @param programs an {@link ImmutableList} of {@link ProgramDefinition}s with the most recent
   *     published versions
   * @return HTML content for rendering the list of available programs
   */
  public Content render(
      Messages messages,
      Http.Request request,
      long applicantId,
      ImmutableList<ProgramDefinition> programs,
      Optional<String> banner) {
    HtmlBundle bundle = layout.getBundle();
    if (banner.isPresent()) {
      bundle.addToastMessages(ToastMessage.alert(banner.get()));
    }
    bundle.addMainContent(
        topContent(
            messages.at(MessageKey.CONTENT_GET_BENEFITS.getKeyName()),
            messages.at(MessageKey.CONTENT_CIVIFORM_DESCRIPTION.getKeyName())),
        mainContent(messages, programs, applicantId, messages.lang().toLocale()));

    return layout.renderWithNav(request, messages, bundle);
  }

  private ContainerTag topContent(String titleText, String infoText) {
    ContainerTag floatTitle =
        div()
            .withId("float-title")
            .withText(titleText)
            .withClasses(
                Styles.RELATIVE, Styles.W_0, Styles.TEXT_6XL, Styles.FONT_SERIF, Styles.FONT_THIN);
    ContainerTag floatText =
        div()
            .withId("float-text")
            .withText(infoText)
            .withClasses(Styles.MY_4, Styles.TEXT_SM, Styles.W_FULL);

    return div()
        .withId("top-content")
        .withClasses(
            Styles.RELATIVE,
            Styles.W_FULL,
            Styles.MB_10,
            StyleUtils.responsiveMedium(Styles.GRID, Styles.GRID_COLS_2))
        .with(floatTitle, floatText);
  }

  private ContainerTag mainContent(
      Messages messages,
      ImmutableList<ProgramDefinition> programs,
      long applicantId,
      Locale preferredLocale) {
    return div()
        .withId("main-content")
        .withClasses(Styles.RELATIVE, Styles.W_FULL, Styles.FLEX, Styles.FLEX_WRAP, Styles.PB_8)
        .with(
            each(
                programs, program -> programCard(messages, program, applicantId, preferredLocale)));
  }

  private ContainerTag programCard(
      Messages messages, ProgramDefinition program, Long applicantId, Locale preferredLocale) {
    String baseId = ReferenceClasses.APPLICATION_CARD + "-" + program.id();

    ContainerTag title =
        div()
            .withId(baseId + "-title")
            .withClasses(Styles.TEXT_LG, Styles.FONT_SEMIBOLD)
            .withText(program.localizedName().getOrDefault(preferredLocale));
    ImmutableList<DomContent> descriptionContent =
        createLinksAndEscapeText(program.localizedDescription().getOrDefault(preferredLocale));
    ContainerTag description =
        div()
            .withId(baseId + "-description")
            .withClasses(Styles.TEXT_XS, Styles.MY_2)
            .with(descriptionContent);

    ContainerTag externalLink =
        new LinkElement()
            .setId(baseId + "-external-link")
            .setStyles(Styles.TEXT_XS, Styles.UNDERLINE)
            .setText(messages.at(MessageKey.CONTENT_PROGRAM_DETAILS.getKeyName()))
            .setHref(routes.DeepLinkController.programByName(program.slug()).url())
            .asAnchorText();
    ContainerTag programData =
        div()
            .withId(baseId + "-data")
            .withClasses(Styles.PX_4)
            .with(title, description, externalLink);

    String applyUrl =
        controllers.applicant.routes.ApplicantProgramsController.edit(applicantId, program.id())
            .url();
    ContainerTag applyButton =
        a().attr(HREF, applyUrl)
            .withText(messages.at(MessageKey.BUTTON_APPLY.getKeyName()))
            .withId(baseId + "-apply")
            .withClasses(ReferenceClasses.APPLY_BUTTON, ApplicantStyles.BUTTON_PROGRAM_APPLY);

    ContainerTag applyDiv =
        div(applyButton).withClasses(Styles.ABSOLUTE, Styles.BOTTOM_6, Styles.W_FULL);
    return div()
        .withId(baseId)
        .withClasses(ReferenceClasses.APPLICATION_CARD, ApplicantStyles.PROGRAM_CARD)
        .with(
            div()
                .withClasses(
                    BaseStyles.BG_SEATTLE_BLUE, Styles.H_3, Styles.ROUNDED_T_XL, Styles.MB_4))
        .with(programData)
        .with(applyDiv);
  }
}
