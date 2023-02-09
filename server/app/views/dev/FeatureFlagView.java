package views.dev;

import static j2html.TagCreator.a;
import static j2html.TagCreator.caption;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import com.google.common.collect.ImmutableMap;
import controllers.dev.routes;
import featureflags.FeatureFlags;
import j2html.tags.Tag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import java.util.stream.Collectors;
import javax.inject.Inject;
import play.mvc.Http.Request;
import play.twirl.api.Content;
import views.BaseHtmlLayout;
import views.BaseHtmlView;
import views.HtmlBundle;
import views.JsBundle;
import views.style.BaseStyles;

public class FeatureFlagView extends BaseHtmlView {

  private final BaseHtmlLayout layout;
  private final FeatureFlags featureFlags;

  @Inject
  public FeatureFlagView(BaseHtmlLayout layout, FeatureFlags featureFlags) {

    this.layout = layout;
    this.featureFlags = featureFlags;
  }

  public Content render(Request request, boolean isDevOrStagingEnvironment) {
    // Create system level control view.
    TableTag serverSettingTable =
        table()
            .with(
                tr().with(
                        td("Server environment: ").withClass("pr-4"),
                        td(Boolean.toString(isDevOrStagingEnvironment))),
                tr().with(
                        td("Configuration: "),
                        td(Boolean.toString(featureFlags.areOverridesEnabled()))));

    // Create per flag view.
    ImmutableMap<String, Boolean> flags = featureFlags.getAllFlags(request);
    var sortedKeys = flags.keySet().stream().sorted().collect(Collectors.toUnmodifiableList());
    TableTag flagTable =
        table()
            .withClass("mt-10")
            .with(
                caption("Current flag values"),
                tr().with(
                        configureCell(th("Flag name")),
                        configureCell(th("Server value")),
                        configureCell(th("Session value")),
                        configureCell(th("Effective value")),
                        configureCell(th("Flip flag"))));

    // For each flag show its config value, session value (if different), the effective value for
    // the user and a control to toggle the value.
    for (String flagName : sortedKeys) {
      Boolean configValue = featureFlags.getFlagEnabledFromConfig(flagName);
      Boolean sessionValue = flags.get(flagName);
      Boolean sessionOverrides = !configValue.equals(sessionValue);
      String sessionDisplay = sessionOverrides ? sessionValue.toString() : "";
      Tag flagFlipLink =
          sessionValue
              ? a().withHref(routes.FeatureFlagOverrideController.disable(flagName).url())
                  .withText("disable")
              : a().withHref(routes.FeatureFlagOverrideController.enable(flagName).url())
                  .withText("enable");
      flagFlipLink.withClasses(BaseStyles.LINK_TEXT, BaseStyles.LINK_HOVER_TEXT);
      flagTable.with(
          tr().with(
                  configureCell(td(flagName)),
                  configureCell(td(configValue.toString())),
                  // If the session value is different highlight that.
                  td(sessionDisplay).withClasses(BaseStyles.TABLE_CELL_STYLES, "font-bold"),
                  // There's no withCondClasses, so leave off TABLE_CELL_STYLE.
                  td(sessionValue.toString()).withCondClass(sessionOverrides, "font-bold"),
                  configureCell(td(flagFlipLink))));
    }

    // Build the page.
    DivTag content =
        div()
            .with(
                h1("Feature Flags").withClasses("py-6"),
                h2("Overrides are allowed if all are true:").withClasses("py-2"))
            .with(serverSettingTable);
    HtmlBundle bundle =
        layout
            .getBundle()
            .setTitle("Feature Flags")
            .addMainContent(content, flagTable)
            .setJsBundle(JsBundle.ADMIN);
    return layout.render(bundle);
  }

  Tag configureCell(Tag tag) {
    return tag.withClasses(BaseStyles.TABLE_CELL_STYLES);
  }
}
