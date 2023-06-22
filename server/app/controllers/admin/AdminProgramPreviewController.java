package controllers.admin;

import auth.Authorizers;
import auth.CiviFormProfile;
import auth.ProfileUtils;
import controllers.CiviFormController;
import java.util.Optional;
import javax.inject.Inject;
import org.pac4j.play.java.Secure;
import play.mvc.Http.Request;
import repository.VersionRepository;
import play.mvc.Result;

public final class AdminProgramPreviewController extends CiviFormController {

  @Inject
  public AdminProgramPreviewController(ProfileUtils profileUtils, VersionRepository versionRepository) {
    super(profileUtils, versionRepository);
  }

  /**
   * Retrieves the admin's user profile and redirects to the application review page where the admin
   * can preview the program.
   */
  @Secure(authorizers = Authorizers.Labels.CIVIFORM_ADMIN)
  public Result preview(Request request, long programId) {
    Optional<CiviFormProfile> profile = profileUtils.currentUserProfile(request);
    if (profile.isEmpty()) {
      throw new RuntimeException("Unable to resolve profile.");
    }

    return redirect(
        controllers.applicant.routes.ApplicantProgramReviewController.review(
            profile.get().getApplicant().get().id, programId));
  }
}
