package services.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import auth.CiviFormProfile;
import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import controllers.BadRequestException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import models.SettingsGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.typedmap.TypedKey;
import play.libs.typedmap.TypedMap;
import play.mvc.Http;
import repository.SettingsGroupRepository;

/**
 * Service management of the resource backed by {@link models.SettingsGroup}.
 *
 * <p>Each time an admin updates the server settings using the admin UI, a SettingsGroup is saved.
 * The latest snapshot is used to provide settings for a given request to the server.
 *
 * <p>On each incoming request, the most recent SettingsGroup is loaded and its settings map stored
 * in the attributes of the incoming {@link play.mvc.Http.Request} object for ease of access
 * throughout the request lifecycle.
 */
public final class SettingsService {

  /** The key used in {@link play.mvc.Http.Request} attributes to store system settings. */
  public static final TypedKey<ImmutableMap<String, String>> CIVIFORM_SETTINGS_ATTRIBUTE_KEY =
      TypedKey.create("CIVIFORM_SETTINGS");

  private static final Logger LOGGER = LoggerFactory.getLogger(SettingsService.class);

  private final SettingsGroupRepository settingsGroupRepository;
  private final SettingsManifest settingsManifest;

  @Inject
  public SettingsService(
      SettingsGroupRepository settingsGroupRepository, SettingsManifest settingsManifest) {
    this.settingsGroupRepository = checkNotNull(settingsGroupRepository);
    this.settingsManifest = checkNotNull(settingsManifest);
  }

  /**
   * Load settings stored in the database. If the admin has never updated any settings this returns
   * an empty map.
   */
  public CompletionStage<Optional<ImmutableMap<String, String>>> loadSettings() {
    return settingsGroupRepository
        .getCurrentSettings()
        .thenApply(maybeSettingsGroup -> maybeSettingsGroup.map(SettingsGroup::getSettings));
  }

  /**
   * Loads the server settings from the database and returns a new request that has the settings in
   * the request attributes. If no settings are found an error is logged and the request argument is
   * returned.
   */
  public CompletionStage<Http.RequestHeader> applySettingsToRequest(Http.RequestHeader request) {
    return loadSettings()
        .thenApply(
            maybeSettings -> {
              if (maybeSettings.isEmpty()) {
                LOGGER.error("No settings found when serving request.");
                return request;
              }

              TypedMap newAttrs =
                  request.attrs().put(CIVIFORM_SETTINGS_ATTRIBUTE_KEY, maybeSettings.get());

              return request.withAttrs(newAttrs);
            });
  }

  /** Update settings stored in the database. */
  public SettingsGroupUpdateResult updateSettings(
      ImmutableMap<String, String> newSettings, CiviFormProfile profile) {
    return updateSettings(newSettings, profile.getAuthorityId().join());
  }

  /**
   * Store a new {@link SettingsGroup} in the DB and returns {@code true} if the new settings are
   * different from the current settings. Otherwise returns {@code false} and does NOT insert a new
   * row.
   */
  public SettingsGroupUpdateResult updateSettings(
      ImmutableMap<String, String> newSettings, String papertrail) {
    var maybeExistingSettings = loadSettings().toCompletableFuture().join();

    if (maybeExistingSettings.map(newSettings::equals).orElse(false)) {
      return SettingsGroupUpdateResult.noChange();
    }

    if (maybeExistingSettings.isPresent()) {
      var validationErrors = validateSettings(newSettings, maybeExistingSettings.get());

      if (!validationErrors.isEmpty()) {
        return SettingsGroupUpdateResult.withErrors(validationErrors);
      }
    }

    var newSettingsGroup = new SettingsGroup(newSettings, papertrail);
    newSettingsGroup.save();

    return SettingsGroupUpdateResult.success();
  }

  private static final ImmutableSet<String> BOOLEAN_VALUES = ImmutableSet.of("true", "false");

  private ImmutableMap<String, SettingsGroupUpdateResult.UpdateError> validateSettings(
      ImmutableMap<String, String> newSettings, ImmutableMap<String, String> existingSettings) {
    ImmutableMap.Builder<String, SettingsGroupUpdateResult.UpdateError> validationErrors =
        ImmutableMap.builder();
    ImmutableList<SettingDescription> settingDescriptions =
        settingsManifest.getAllAdminWriteableSettingDescriptions();

    Maps.difference(newSettings, existingSettings).entriesDiffering().entrySet().stream()
        .forEach(
            entry -> {
              String variableName = entry.getKey();
              SettingDescription settingDescription =
                  settingDescriptions.stream()
                      .filter((sd) -> sd.variableName().equals(variableName))
                      .findFirst()
                      .orElseThrow();
              String newValue = entry.getValue().leftValue();

              switch (settingDescription.settingType()) {
                case BOOLEAN:
                  {
                    if (!BOOLEAN_VALUES.contains(newValue)) {
                      throw new BadRequestException(
                          String.format("Invalid boolean value: %s", newValue));
                    }
                    break;
                  }

                case STRING:
                  {
                    Optional<SettingsGroupUpdateResult.UpdateError> error =
                        validateString(settingDescription, newValue);

                    if (error.isPresent()) {
                      validationErrors.put(settingDescription.variableName(), error.get());
                    }
                    break;
                  }

                default:
                  throw new IllegalStateException(
                      String.format(
                          "Settings of type %s are not writeable",
                          settingDescription.settingType()));
              }
            });

    return validationErrors.build();
  }

  private static Optional<SettingsGroupUpdateResult.UpdateError> validateString(
      SettingDescription settingDescription, String value) {
    if (settingDescription.allowableValues().isPresent()
        && !settingDescription.allowableValues().get().contains(value)) {
      throw new BadRequestException(
          String.format(
              "Invalid enum value: %s, must be one of %s",
              value, Joiner.on(", ").join(settingDescription.allowableValues().get())));
    }

    if (settingDescription.validationRegex().isPresent()
        && !settingDescription.validationRegex().get().asMatchPredicate().test(value)) {
      return Optional.of(
          SettingsGroupUpdateResult.UpdateError.create(
              value,
              String.format(
                  "Invalid input, must match %s", settingDescription.validationRegex().get())));
    }

    return Optional.empty();
  }

  /**
   * Inserts a new {@link SettingsGroup} if it finds admin writeable settings in the {@link
   * SettingsManifest} that are not in the current {@link SettingsGroup}.
   */
  public SettingsGroup migrateConfigValuesToSettingsGroup() {
    Optional<SettingsGroup> maybeExistingSettingsGroup =
        settingsGroupRepository.getCurrentSettings().toCompletableFuture().join();
    Optional<ImmutableMap<String, String>> maybeExistingSettings =
        maybeExistingSettingsGroup.map(SettingsGroup::getSettings);

    ImmutableMap.Builder<String, String> settingsBuilder = ImmutableMap.builder();

    for (var settingDescription : settingsManifest.getAllAdminWriteableSettingDescriptions()) {
      maybeExistingSettings
          .flatMap(
              existingSettings ->
                  Optional.ofNullable(existingSettings.get(settingDescription.variableName())))
          .ifPresentOrElse(
              existingValue ->
                  settingsBuilder.put(settingDescription.variableName(), existingValue),
              () ->
                  settingsManifest
                      .getSettingSerializationValue(settingDescription)
                      .ifPresent(
                          value -> settingsBuilder.put(settingDescription.variableName(), value)));
    }

    var settings = settingsBuilder.build();

    if (maybeExistingSettings.map(settings::equals).orElse(false)) {
      return maybeExistingSettingsGroup.get();
    }

    var group = new SettingsGroup(settings, "system");
    group.save();

    LOGGER.info("Migrated {} settings from config to database.", settings.size());

    return group;
  }

  @AutoValue
  public abstract static class SettingsGroupUpdateResult {

    public static SettingsGroupUpdateResult success() {
      return new AutoValue_SettingsService_SettingsGroupUpdateResult(
          /* errorMessages= */ Optional.empty(), /* updated= */ true);
    }

    public static SettingsGroupUpdateResult withErrors(
        ImmutableMap<String, UpdateError> errorMessages) {
      return new AutoValue_SettingsService_SettingsGroupUpdateResult(
          Optional.of(errorMessages), /* updated= */ false);
    }

    public static SettingsGroupUpdateResult noChange() {
      return new AutoValue_SettingsService_SettingsGroupUpdateResult(
          /* errorMessages= */ Optional.empty(), /* updated= */ false);
    }

    public abstract Optional<ImmutableMap<String, UpdateError>> errorMessages();

    public abstract boolean updated();

    public boolean hasErrors() {
      return errorMessages().isPresent();
    }

    @AutoValue
    public abstract static class UpdateError {

      public static UpdateError create(String updatedValue, String errorMessage) {
        return new AutoValue_SettingsService_SettingsGroupUpdateResult_UpdateError(
            updatedValue, errorMessage);
      }

      public abstract String updatedValue();

      public abstract String errorMessage();
    }
  }
}
