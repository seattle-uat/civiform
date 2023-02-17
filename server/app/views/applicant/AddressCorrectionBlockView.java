package views.applicant;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.*;

import com.google.common.collect.ImmutableList;
import j2html.TagCreator;
import j2html.tags.specialized.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.twirl.api.Content;
import controllers.applicant.routes;
import services.Address;
import services.geo.AddressSuggestion;
import services.geo.AddressSuggestionGroup;
import services.MessageKey;
import views.ApplicationBaseView;
import views.HtmlBundle;
import views.components.Icons;
import views.components.LinkElement;
import views.questiontypes.ApplicantQuestionRendererParams;
import views.style.ApplicantStyles;
import views.style.StyleUtils;

/** Renders a page indicating the applicant is not eligible for a program. */
public final class AddressCorrectionBlockView extends ApplicationBaseView {
  private final String BLOCK_FORM_ID = "cf-block-form";
  private static int MAX_SUGGESTIONS_TO_DISPLAY = 3;
  private final ApplicantLayout layout;
  public static String USER_KEEPING_ADDRESS_VALUE = "USER_KEEPING_ADDRESS_VALUE";
  public static String SELECTED_ADDRESS_NAME = "selectedAddress";

  @Inject
  AddressCorrectionBlockView(ApplicantLayout layout) {
    this.layout = checkNotNull(layout);
  }

  public Content render(
    Params params,
    Messages messages,
    Address addressAsEntered,
    ImmutableList<services.geo.AddressSuggestion> suggestions) {

    DivTag content = div()
      .withClass("my-8 m-auto")
      .with(renderForm(params, messages, addressAsEntered, suggestions));

    HtmlBundle bundle =
      layout
        .getBundle()
        .setTitle(layout.renderPageTitleWithBlockProgress(params.programTitle(), params.blockIndex(), params.totalBlockCount()))
        .addMainStyles(ApplicantStyles.MAIN_PROGRAM_APPLICATION)
        .addMainContent(
            layout.renderProgramApplicationTitleAndProgressIndicator(params.programTitle(), params.blockIndex(), params.totalBlockCount(), false),
            content
        );

    return layout.renderWithNav(params.request(), params.applicantName(), messages, bundle);
  }

  private FormTag renderForm(Params params, Messages messages, Address addressAsEntered, ImmutableList<AddressSuggestion> suggestions) {
    String formAction = routes.ApplicantProgramBlocksController.confirmAddress(params.applicantId(), params.programId(), params.block().getId()).url();

    FormTag form = form().withId(BLOCK_FORM_ID)
      .withAction(formAction)
      .withMethod(Http.HttpVerbs.POST)
      .with(makeCsrfTokenInputTag(params.request()));

    form.with(h2(messages.at(MessageKey.ADDRESS_CORRECTION_SUB_HEADING.getKeyName())).withClass("font-bold mb-2"));
    form.with(div(messages.at(MessageKey.ADDRESS_CORRECTION_PAGE_INSTRUCTIONS.getKeyName())).withClass("mb-8"));

    form
      .with(
        div()
          .withClasses("mb-8")
          .with(
            renderAsEnteredHeading(params.applicantId(), params.programId(), params.block().getId(), messages),
            renderAddress(addressAsEntered, false, Optional.empty())),
        h3(messages.at(MessageKey.ADDRESS_CORRECTION_SUGGESTIONS_HEADING.getKeyName())).withClass("font-bold mb-2"))
      .with(
        div()
          .withClasses("mb-8")
          .with(renderSuggestedAddresses(suggestions)))
      .with(renderBottomNavButtons(params));

    return form;
  }

  private DivTag renderAsEnteredHeading(long applicantId, long programId, String blockId, Messages messages) {
    var containerDiv = div().withClass("flex flex-nowrap mb-2");

    ATag editElement =
      new LinkElement()
        .setStyles("bottom-0", "right-0", "text-blue-600", StyleUtils.hover("text-blue-700"), "mb-2")
        .setHref(routes.ApplicantProgramBlocksController.review(applicantId, programId, blockId).url())
        .setText(messages.at(MessageKey.LINK_EDIT.getKeyName()))
        .setIcon(Icons.EDIT, LinkElement.IconPosition.START)
        .asAnchorText();

    containerDiv.with(
      h3(messages.at(MessageKey.ADDRESS_CORRECTION_AS_ENTERED_HEADING.getKeyName())).withClass("font-bold mb-2 w-full"),
      editElement
    );

    return containerDiv;
  }

  private ImmutableList<LabelTag> renderSuggestedAddresses(ImmutableList<AddressSuggestion> suggestions) {
    boolean selected = true;
    List<LabelTag> addressLabels = new ArrayList<>();

    int maxSuggestions = Math.min(suggestions.size(), MAX_SUGGESTIONS_TO_DISPLAY);

    for (int i = 0; i < maxSuggestions; i++) {
      AddressSuggestion suggestion = suggestions.get(i);
      addressLabels.add(renderAddress(suggestion.getAddress(), selected, Optional.ofNullable(suggestion.getSingleLineAddress())));
      selected = false;
    }

    return ImmutableList.copyOf(addressLabels);
  }

  private LabelTag renderAddress(Address address, boolean selected, Optional<String> singleLineAddress) {
    var containerDiv = label().withClass("flex flex-nowrap mb-2");

    var input = TagCreator.input()
      .withType("radio")
      .withName(SELECTED_ADDRESS_NAME)
      .withClass("cf-radio-input h-4 w-4 mr-4 align-middle");

    if (singleLineAddress.isPresent()) {
      input.withValue(singleLineAddress.get());
    } else {
      input.withValue(USER_KEEPING_ADDRESS_VALUE);
    }

    if (selected) {
      input.attr("checked", "checked");
    }

    containerDiv.with(div().with(input));

    var addressDiv = div().with(div(address.getStreet()));

    if (address.hasLine2()) {
      addressDiv.with(div(address.getLine2()));
    }

    addressDiv.with(div(
      String.format("%s, %s %s",
        address.getCity(),
        address.getState(),
        address.getZip()
    )));

    containerDiv.with(addressDiv);

    return containerDiv;
  }

  private DivTag renderBottomNavButtons(Params params) {
    return div()
      .withClasses(ApplicantStyles.APPLICATION_NAV_BAR)
      // An empty div to take up the space to the left of the buttons.
      .with(div().withClasses("flex-grow"))
      .with(renderReviewButton(params))
      .with(renderPreviousButton(params))
      .with(renderNextButton(params));
  }

  private ButtonTag renderNextButton(Params params) {
    return submitButton(params.messages().at(MessageKey.BUTTON_NEXT_SCREEN.getKeyName()))
      .withClasses(ApplicantStyles.BUTTON_BLOCK_NEXT)
      .withId("cf-block-submit");
  }
}
