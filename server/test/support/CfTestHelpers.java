package support;

import static org.mockito.Mockito.mockStatic;
import static services.settings.SettingsService.CIVIFORM_SETTINGS_ATTRIBUTE_KEY;

import com.google.common.collect.ImmutableMap;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javax.inject.Provider;
import org.mockito.MockedStatic;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import play.api.test.Helpers;
import play.mvc.Call;
import play.mvc.Http;
import repository.AccountRepository;
import services.program.predicate.PredicateValue;

public class CfTestHelpers {

  // ErrorProne raises a warning about the return value from
  // mockedStatic.when(Instant::now).thenReturn(instant)
  // not being used unless suppressed.
  @SuppressWarnings("ReturnValueIgnored")
  public static void withMockedInstantNow(String instantString, Runnable fn) {
    Clock clock = Clock.fixed(Instant.parse(instantString), ZoneId.of("UTC"));
    Instant instant = Instant.now(clock);

    try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
      mockedStatic.when(Instant::now).thenReturn(instant);
      fn.run();
    }
  }

  public static ImmutableMap<String, Object> oidcConfig(String host, int port) {
    return new ImmutableMap.Builder<String, Object>()
        .put("civiform_applicant_idp", "idcs")
        .put("idcs.client_id", "idcs-fake-oidc-client")
        .put("idcs.secret", "idcs-fake-oidc-secret")
        .put(
            "idcs.discovery_uri",
            String.format("http://%s:%d/.well-known/openid-configuration", host, port))
        .put("base_url", String.format("http://localhost:%d", Helpers.testServerPort()))
        .put("auth.oidc_post_logout_param", "post_logout_redirect_uri")
        .put(
            "auth.applicant_oidc_override_logout_url",
            String.format("http://%s:%d/session/end", host, port))
        .put("auth.oidc_provider_logout", true)
        .build();
  }

  public static Provider<AccountRepository> userRepositoryProvider(
      AccountRepository accountRepository) {
    return new Provider<AccountRepository>() {
      @Override
      public AccountRepository get() {
        return accountRepository;
      }
    };
  }

  public static OidcConfiguration getOidcConfiguration(String host, int port) {
    OidcConfiguration config = new OidcConfiguration();
    config.setClientId("idcs-fake-oidc-client");
    config.setSecret("idcs-fake-oidc-secret");
    config.setDiscoveryURI(
        String.format("http://%s:%d/.well-known/openid-configuration", host, port));

    // Tells the OIDC provider what type of response to use when it sends info back
    // from the auth request.
    config.setResponseMode("form_post");
    config.setResponseType("id_token");

    config.setUseNonce(true);
    config.setWithState(false);

    config.setScope("openid profile email");
    return config;
  }

  public static OidcClient getOidcClient(String host, int port) {
    OidcConfiguration config = getOidcConfiguration(host, port);
    return new OidcClient(config);
  }

  public static PredicateValue stringToPredicateDate(String rawDate) {
    LocalDate localDate = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return PredicateValue.of(localDate);
  }

  public static final Http.Request EMPTY_REQUEST = play.test.Helpers.fakeRequest().build();

  public static Http.RequestBuilder requestBuilderWithSettings(Call call, String... settings) {
    return CfTestHelpers.requestBuilderWithSettings(play.test.Helpers.fakeRequest(call), settings);
  }

  public static Http.RequestBuilder requestBuilderWithSettings(String... settings) {
    return CfTestHelpers.requestBuilderWithSettings(play.test.Helpers.fakeRequest(), settings);
  }

  public static Http.RequestBuilder requestBuilderWithSettings(
      Http.RequestBuilder requestBuilder, String... settings) {
    if (settings.length % 2 != 0) {
      throw new IllegalArgumentException(
          String.format(
              "Odd number of args specified for settings map, must be key-value pairs: %s",
              Arrays.toString(settings)));
    }

    ImmutableMap.Builder<String, String> settingsMap = ImmutableMap.builder();

    for (int i = 0; i < settings.length; i += 2) {
      settingsMap.put(settings[i], settings[i + 1]);
    }

    return requestBuilder.attr(CIVIFORM_SETTINGS_ATTRIBUTE_KEY, settingsMap.build());
  }
}
