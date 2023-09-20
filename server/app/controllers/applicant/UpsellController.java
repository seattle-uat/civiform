package controllers.applicant;

import static com.google.common.base.Preconditions.checkNotNull;

import auth.CiviFormProfile;
import auth.ProfileUtils;
import com.google.common.collect.ImmutableList;
import controllers.CiviFormController;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.Account;
import models.Application;
import org.pac4j.play.java.Secure;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import repository.VersionRepository;
import services.applicant.ApplicantPersonalInfo;
import services.applicant.ApplicantService;
import services.applicant.ApplicantService.ApplicantProgramData;
import services.applicant.ReadOnlyApplicantProgramService;
import services.applications.ApplicationService;
import services.applications.PdfExporterService;
import services.export.PdfExporter;
import services.program.ProgramDefinition;
import services.program.ProgramNotFoundException;
import services.program.ProgramService;
import views.applicant.ApplicantCommonIntakeUpsellCreateAccountView;
import views.applicant.ApplicantUpsellCreateAccountView;
import views.components.ToastMessage;

/** Controller for handling methods for upselling applicants. */
public final class UpsellController extends CiviFormController {

  private final HttpExecutionContext httpContext;
  private final ApplicantService applicantService;
  private final ApplicationService applicationService;
  private final ProgramService programService;
  private final ApplicantUpsellCreateAccountView upsellView;
  private final ApplicantCommonIntakeUpsellCreateAccountView cifUpsellView;
  private final MessagesApi messagesApi;
  private final PdfExporterService pdfExporterService;

  @Inject
  public UpsellController(
      HttpExecutionContext httpContext,
      ApplicantService applicantService,
      ApplicationService applicationService,
      ProfileUtils profileUtils,
      ProgramService programService,
      ApplicantUpsellCreateAccountView upsellView,
      ApplicantCommonIntakeUpsellCreateAccountView cifUpsellView,
      MessagesApi messagesApi,
      PdfExporterService pdfExporterService,
      VersionRepository versionRepository) {
    super(profileUtils, versionRepository);
    this.httpContext = checkNotNull(httpContext);
    this.applicantService = checkNotNull(applicantService);
    this.applicationService = checkNotNull(applicationService);
    this.programService = checkNotNull(programService);
    this.upsellView = checkNotNull(upsellView);
    this.cifUpsellView = checkNotNull(cifUpsellView);
    this.messagesApi = checkNotNull(messagesApi);
    this.pdfExporterService = checkNotNull(pdfExporterService);
  }

  @Secure
  public CompletionStage<Result> considerRegister(
      Http.Request request,
      long applicantId,
      long programId,
      long applicationId,
      String redirectTo) {
    Optional<CiviFormProfile> profile = profileUtils.currentUserProfile(request);
    if (profile.isEmpty()) {
      // should definitely never happen.
      return CompletableFuture.completedFuture(
          badRequest("You are not signed in - you cannot perform this action."));
    }

    CompletableFuture<Boolean> isCommonIntake =
        programService
            .getProgramDefinitionAsync(programId)
            .thenApplyAsync(ProgramDefinition::isCommonIntakeForm)
            .toCompletableFuture();

    CompletableFuture<ApplicantPersonalInfo> applicantPersonalInfo =
        applicantService.getPersonalInfo(applicantId).toCompletableFuture();

    CompletableFuture<Account> account =
        applicantPersonalInfo
            .thenComposeAsync(
                v -> checkApplicantAuthorization(request, applicantId), httpContext.current())
            .thenComposeAsync(v -> profile.get().getAccount(), httpContext.current())
            .toCompletableFuture();

    CompletableFuture<ReadOnlyApplicantProgramService> roApplicantProgramService =
        applicantService
            .getReadOnlyApplicantProgramService(applicantId, programId)
            .toCompletableFuture();

    return CompletableFuture.allOf(isCommonIntake, account, roApplicantProgramService)
        .thenComposeAsync(
            ignored -> {
              if (!isCommonIntake.join()) {
                // If this isn't the common intake form, we don't need to make the
                // call to get the applicant's eligible programs.
                Optional<ImmutableList<ApplicantProgramData>> result = Optional.empty();
                return CompletableFuture.completedFuture(result);
              }

              return applicantPersonalInfo
                  .thenComposeAsync(v -> checkApplicantAuthorization(request, applicantId))
                  .thenComposeAsync(
                      // we are already checking if profile is empty
                      v ->
                          applicantService.maybeEligibleProgramsForApplicant(
                              applicantId, profile.get()),
                      httpContext.current())
                  .thenApplyAsync(Optional::of);
            })
        .thenApplyAsync(
            maybeEligiblePrograms -> {
              Optional<ToastMessage> toastMessage =
                  request.flash().get("banner").map(m -> ToastMessage.alert(m));

              if (isCommonIntake.join()) {
                return ok(
                    cifUpsellView.render(
                        request,
                        redirectTo,
                        account.join(),
                        applicantPersonalInfo.join(),
                        applicantId,
                        programId,
                        profileUtils
                            .currentUserProfile(request)
                            .orElseThrow()
                            .isTrustedIntermediary(),
                        maybeEligiblePrograms.orElseGet(ImmutableList::of),
                        messagesApi.preferred(request),
                        toastMessage));
              }

              return ok(
                  upsellView.render(
                      request,
                      redirectTo,
                      account.join(),
                      roApplicantProgramService.join().getApplicantData().preferredLocale(),
                      roApplicantProgramService.join().getProgramTitle(),
                      roApplicantProgramService.join().getCustomConfirmationMessage(),
                      applicantPersonalInfo.join(),
                      programId,
                      applicantId,
                      applicationId,
                      messagesApi.preferred(request),
                      toastMessage));
            },
            httpContext.current())
        .exceptionally(
            ex -> {
              if (ex instanceof CompletionException) {
                Throwable cause = ex.getCause();
                if (cause instanceof SecurityException) {
                  return unauthorized();
                }
                if (cause instanceof ProgramNotFoundException) {
                  return notFound(cause.toString());
                }
              }
              throw new RuntimeException(ex);
            });
  }

  /** Download a PDF file of the application to the program. */
  @Secure
  public CompletionStage<Result> download(
      Http.Request request, long programId, long applicationId, long applicantId)
      throws ProgramNotFoundException {
    CompletableFuture<ProgramDefinition> program =
        programService.getProgramDefinitionAsync(programId).toCompletableFuture();
    Optional<CiviFormProfile> profileMaybe = profileUtils.currentUserProfile(request);
    CiviFormProfile profile =
        profileMaybe.orElseThrow(
            () -> new NoSuchElementException("User authorized as applicant but no profile found"));

    CompletableFuture<Account> account =
        program
            .thenComposeAsync(
                v -> checkApplicantAuthorization(request, applicantId), httpContext.current())
            .thenComposeAsync(v -> profile.getAccount(), httpContext.current())
            .toCompletableFuture();

    CompletableFuture<Optional<Application>> applicationMaybe =
        applicationService.getApplicationAsync(applicationId).toCompletableFuture();

    return CompletableFuture.allOf(applicationMaybe, account)
        .thenApplyAsync(
            check -> {
              if (!profile.isTrustedIntermediary()) {
                if (!applicationMaybe.join().get().getApplicant().getAccount().equals(account.join())) {
                  return unauthorized();
                }
              }
              PdfExporter.InMemoryPdf pdf =
                  pdfExporterService.generatePdf(applicationMaybe.join().get());

              return ok(pdf.getByteArray())
                  .as("application/pdf")
                  .withHeader(
                      "Content-Disposition",
                      String.format("attachment; filename=\"%s\"", pdf.getFileName()));
            })
        .exceptionally(
            ex -> {
              if (ex instanceof CompletionException) {
                Throwable cause = ex.getCause();
                if (cause instanceof SecurityException) {
                  return unauthorized(cause.toString());
                }
                if (cause instanceof ProgramNotFoundException) {
                  return notFound(cause.toString());
                }
              }
              throw new RuntimeException(ex);
            });
  }
}
