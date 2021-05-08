package auth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import javax.inject.Inject;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.credentials.AnonymousCredentials;
import org.pac4j.core.util.HttpActionHelper;

public class GuestClient extends IndirectClient {

  public static final String CLIENT_NAME = "GuestClient";

  private ProfileFactory profileFactory;

  @Inject
  public GuestClient(ProfileFactory profileFactory) {
    this.profileFactory = checkNotNull(profileFactory);
  }

  @Override
  protected void internalInit() {

    /*
     * This is the root of the non-logged-in auth story.  This class is invoked to
     * provide a profile for a user who hits /callback?client_name=GuestClient.
     * It extracts "credentials" from the request (the creds are of since
     * the user is not logged in) and "authenticates" them, loading a profile
     * which is generated by the ProfileFactory.  Then it redirects the user
     * to the home page.
     */
    defaultCredentialsExtractor((ctx, store) -> Optional.of(new AnonymousCredentials()));
    defaultAuthenticator(
        (cred, ctx, store) -> cred.setUserProfile(profileFactory.createNewApplicant()));
    defaultRedirectionActionBuilder(
        (ctx, store) -> Optional.of(HttpActionHelper.buildRedirectUrlAction(ctx, "/")));
  }
}
