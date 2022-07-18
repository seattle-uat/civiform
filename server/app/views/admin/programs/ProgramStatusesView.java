package views.admin.programs;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.input;
import static j2html.TagCreator.p;
import static j2html.TagCreator.span;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import controllers.admin.routes;
import j2html.tags.specialized.ButtonTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.FormTag;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import play.mvc.Http;
import play.twirl.api.Content;
import services.LocalizedStrings;
import services.program.ProgramDefinition;
import services.program.StatusDefinitions;
import views.BaseHtmlView;
import views.HtmlBundle;
import views.admin.AdminLayout;
import views.admin.AdminLayout.NavPage;
import views.admin.AdminLayoutFactory;
import views.components.FieldWithLabel;
import views.components.Icons;
import views.components.Modal;
import views.components.Modal.Width;
import views.components.ToastMessage;
import views.style.AdminStyles;
import views.style.StyleUtils;
import views.style.Styles;

public final class ProgramStatusesView extends BaseHtmlView {
  public static final String ORIGINAL_STATUS_TEXT_FORM_NAME = "original_status_text";
  public static final String STATUS_TEXT_FORM_NAME = "status_text";
  public static final String EMAIL_BODY_FORM_NAME = "email_body";

  private final AdminLayout layout;

  @Inject
  public ProgramStatusesView(AdminLayoutFactory layoutFactory) {
    this.layout = checkNotNull(layoutFactory).getLayout(NavPage.PROGRAMS);
  }

  public Content render(Http.Request request, ProgramDefinition program) {
    Modal createStatusModal = makeStatusEditModal(request, program, Optional.empty());
    ButtonTag createStatusTriggerButton =
        makeSvgTextButton("Create a new status", Icons.PLUS)
            .withClasses(AdminStyles.SECONDARY_BUTTON_STYLES, Styles.MY_2)
            .withId(createStatusModal.getTriggerButtonId());

    Pair<DivTag, ImmutableList<Modal>> statusContainerAndModals =
        renderStatusContainer(request, program);

    DivTag contentDiv =
        div()
            .withClasses(Styles.PX_4)
            .with(
                div()
                    .withClasses(
                        Styles.FLEX,
                        Styles.ITEMS_CENTER,
                        Styles.SPACE_X_4,
                        Styles.MT_12,
                        Styles.MB_10)
                    .with(
                        h1(
                            String.format(
                                "Manage application statuses for %s", program.adminName())),
                        div().withClass(Styles.FLEX_GROW),
                        renderManageTranslationsLink(program),
                        createStatusTriggerButton),
                statusContainerAndModals.getLeft());

    HtmlBundle htmlBundle =
        layout
            .getBundle()
            .setTitle("Manage program statuses")
            .addMainContent(contentDiv)
            .addModals(createStatusModal)
            .addModals(statusContainerAndModals.getRight());

    Http.Flash flash = request.flash();
    if (flash.get("error").isPresent()) {
      htmlBundle.addToastMessages(ToastMessage.error(flash.get("error").get()).setDuration(-1));
    } else if (flash.get("success").isPresent()) {
      htmlBundle.addToastMessages(ToastMessage.success(flash.get("success").get()).setDuration(-1));
    }

    return layout.renderCentered(htmlBundle);
  }

  private ButtonTag renderManageTranslationsLink(ProgramDefinition program) {
    String linkDestination =
        routes.AdminProgramTranslationsController.edit(
                program.id(), LocalizedStrings.DEFAULT_LOCALE.toLanguageTag())
            .url();
    ButtonTag button =
        makeSvgTextButton("Manage translations", Icons.LANGUAGE)
            .withClass(AdminStyles.SECONDARY_BUTTON_STYLES);
    return asRedirectButton(button, linkDestination);
  }

  private Pair<DivTag, ImmutableList<Modal>> renderStatusContainer(
      Http.Request request, ProgramDefinition program) {
    ImmutableList<StatusDefinitions.Status> statuses = program.statusDefinitions().getStatuses();
    String numResultsText =
        statuses.size() == 1 ? "1 result" : String.format("%d results", statuses.size());
    ImmutableList<Pair<DivTag, ImmutableList<Modal>>> statusTagsAndModals =
        statuses.stream()
            .map(s -> renderStatusItem(request, program, s))
            .collect(ImmutableList.toImmutableList());
    return Pair.of(
        div()
            .with(
                p(numResultsText),
                div()
                    .withClasses(Styles.MT_6, Styles.BORDER, Styles.ROUNDED_MD, Styles.DIVIDE_Y)
                    .with(statusTagsAndModals.stream().map(Pair::getLeft))
                    .condWith(
                        statuses.isEmpty(),
                        p("No statuses have been created yet")
                            .withClasses(Styles.ML_4, Styles.MY_4))),
        statusTagsAndModals.stream()
            .map(Pair::getRight)
            .flatMap(Collection::stream)
            .collect(ImmutableList.toImmutableList()));
  }

  private Pair<DivTag, ImmutableList<Modal>> renderStatusItem(
      Http.Request request, ProgramDefinition program, StatusDefinitions.Status status) {
    Modal editStatusModal = makeStatusEditModal(request, program, Optional.of(status));
    ButtonTag editStatusTriggerButton =
        makeSvgTextButton("Edit", Icons.EDIT)
            .withClass(AdminStyles.TERTIARY_BUTTON_STYLES)
            .withId(editStatusModal.getTriggerButtonId());

    Modal deleteStatusModal = makeStatusDeleteModal(request, program, status);
    ButtonTag deleteStatusTriggerButton =
        makeSvgTextButton("Delete", Icons.DELETE)
            .withClass(AdminStyles.TERTIARY_BUTTON_STYLES)
            .withId(deleteStatusModal.getTriggerButtonId());
    return Pair.of(
        div()
            .withClasses(
                Styles.PL_7,
                Styles.PR_6,
                Styles.PY_9,
                Styles.FONT_NORMAL,
                Styles.SPACE_X_2,
                Styles.FLEX,
                Styles.ITEMS_CENTER,
                StyleUtils.hover(Styles.BG_GRAY_100))
            .with(
                div()
                    .withClass(Styles.W_1_4)
                    .with(
                        // TODO(#2752): Optional SVG icon for status attribute.
                        span(status.statusText()).withClasses(Styles.ML_2, Styles.BREAK_WORDS)),
                div()
                    .condWith(
                        status.emailBodyText().isPresent(),
                        p().withClasses(
                                Styles.MT_1, Styles.TEXT_XS, Styles.FLEX, Styles.ITEMS_CENTER)
                            .with(
                                Icons.svg(Icons.EMAIL, 22)
                                    // TODO(#2752): Once SVG icon sizes are consistent, just set
                                    // size to 18.
                                    .withWidth("18")
                                    .withHeight("18")
                                    .withClasses(Styles.MR_2, Styles.INLINE_BLOCK),
                                span("Applicant notification email added"))),
                div().withClass(Styles.FLEX_GROW),
                deleteStatusTriggerButton,
                editStatusTriggerButton),
        ImmutableList.of(editStatusModal, deleteStatusModal));
  }

  private Modal makeStatusDeleteModal(
      Http.Request request, ProgramDefinition program, StatusDefinitions.Status toDelete) {
    DivTag content =
        div()
            .withClasses(Styles.PX_6, Styles.PY_2)
            .with(
                p(
                    "Warning: This will also remove any translated content for the status and"
                        + " email body."),
                form()
                    .withMethod("POST")
                    .withAction(routes.AdminProgramStatusesController.delete(program.id()).url())
                    .with(
                        makeCsrfTokenInputTag(request),
                        input().isHidden().withName("status_text").withValue(toDelete.statusText()),
                        div()
                            .withClasses(Styles.FLEX, Styles.MT_5, Styles.SPACE_X_2)
                            .with(
                                div().withClass(Styles.FLEX_GROW),
                                submitButton("Delete")
                                    .withClass(AdminStyles.SECONDARY_BUTTON_STYLES))));

    return Modal.builder(randomModalId(), content).setModalTitle("Delete this status").build();
  }

  private static String randomModalId() {
    // We prepend a "a-" since element IDs must start with an alphabetic character, whereas UUIDs
    // can start with a numeric character.
    return "a-" + UUID.randomUUID().toString();
  }

  private Modal makeStatusEditModal(
      Http.Request request, ProgramDefinition program, Optional<StatusDefinitions.Status> status) {
    String emailBody =
        status.map(StatusDefinitions.Status::emailBodyText).orElse(Optional.empty()).orElse("");
    FormTag content =
        form()
            .withMethod("POST")
            .withAction(routes.AdminProgramStatusesController.edit(program.id()).url())
            .withClasses(Styles.PX_6, Styles.PY_2)
            .with(
                makeCsrfTokenInputTag(request),
                input()
                    .isHidden()
                    .withName(ORIGINAL_STATUS_TEXT_FORM_NAME)
                    .withValue(status.map(StatusDefinitions.Status::statusText).orElse("")),
                FieldWithLabel.input()
                    .setFieldName(STATUS_TEXT_FORM_NAME)
                    .setLabelText("Status name (required)")
                    // TODO(#2752): Potentially move placeholder text to an actual
                    // description.
                    .setPlaceholderText("Enter status name here")
                    .setValue(status.map(StatusDefinitions.Status::statusText))
                    .getInputTag(),
                div()
                    .withClasses(Styles.PT_8)
                    .with(
                        FieldWithLabel.textArea()
                            .setFieldName(EMAIL_BODY_FORM_NAME)
                            .setLabelText("Applicant status change email")
                            .setPlaceholderText("Notify the Applicant about the status change")
                            .setRows(OptionalLong.of(5))
                            .setValue(emailBody)
                            .getTextareaTag()),
                div()
                    .withClasses(Styles.FLEX, Styles.MT_5, Styles.SPACE_X_2)
                    .with(
                        div().withClass(Styles.FLEX_GROW),
                        // TODO(#2752): Add a cancel button that clears state.
                        submitButton("Confirm").withClass(AdminStyles.TERTIARY_BUTTON_STYLES)));
    return Modal.builder(randomModalId(), content)
        .setModalTitle(status.isPresent() ? "Edit this status" : "Create a new status")
        .setWidth(Width.HALF)
        .build();
  }
}
