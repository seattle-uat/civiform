package auth;

import static controllers.CallbackController.REDIRECT_TO_SESSION_KEY;
import static play.mvc.Results.redirect;

import controllers.routes;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Contains methods related to redirecting for the purpose of creating guest accounts for new users.
 */
public final class DefaultToGuestRedirector {

  /**
   * This method is used when the user's profile is empty, and we want to create a guest profile for
   * them, redirecting them to the original page afterward.
   */
  public static Result createGuestSessionAndRedirect(Http.Request request) {
    System.out.println(
        "XXX before creating guest session, the current session contains: "
            + request.session().data());
    return redirect(routes.CallbackController.callback(GuestClient.CLIENT_NAME).url())
        .addingToSession(request, REDIRECT_TO_SESSION_KEY, request.uri());
    // .withSession(ImmutableMap.of(REDIRECT_TO_SESSION_KEY, request.uri()));
  }
}
