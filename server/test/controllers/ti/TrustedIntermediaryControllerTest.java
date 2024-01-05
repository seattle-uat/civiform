package controllers.ti;

import static org.assertj.core.api.Assertions.assertThat;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.fakeRequest;

import auth.ProfileFactory;
import auth.ProfileUtils;
import com.google.common.collect.ImmutableMap;
import controllers.WithMockedProfiles;
import io.ebean.DB;
import io.ebean.Database;
import java.util.Optional;
import models.AccountModel;
import models.ApplicantModel;
import models.LifecycleStage;
import models.Models;
import models.TrustedIntermediaryGroupModel;
import models.VersionModel;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import repository.AccountRepository;
import services.applicant.exception.ApplicantNotFoundException;
import services.settings.SettingsService;

public class TrustedIntermediaryControllerTest extends WithMockedProfiles {
  private AccountRepository repo;
  private TrustedIntermediaryController tiController;
  private ProfileFactory profileFactory;
  private ProfileUtils profileUtils;

  @Before
  public void setup() {
    Database database = DB.getDefault();
    Models.truncate(database);
    VersionModel newActiveVersion = new VersionModel(LifecycleStage.ACTIVE);
    newActiveVersion.save();
    instanceOf(SettingsService.class).migrateConfigValuesToSettingsGroup();
    profileFactory = instanceOf(ProfileFactory.class);
    tiController = instanceOf(TrustedIntermediaryController.class);
    ApplicantModel managedApplicant = createApplicant();
    createTIWithMockedProfile(managedApplicant);
    repo = instanceOf(AccountRepository.class);
    profileFactory.createFakeTrustedIntermediary();
    profileUtils = instanceOf(ProfileUtils.class);
  }

  @Test
  public void testUpdateClientInfo() throws ApplicantNotFoundException {
    Http.RequestBuilder requestBuilder =
        addCSRFToken(
            Helpers.fakeRequest()
                .bodyForm(
                    ImmutableMap.of(
                        "firstName",
                        "first",
                        "middleName",
                        "middle",
                        "lastName",
                        "last",
                        "emailAddress",
                        "sample2@fake.com",
                        "dob",
                        "2022-07-18")));

    TrustedIntermediaryGroupModel trustedIntermediaryGroup =
        repo.getTrustedIntermediaryGroup(
                profileUtils.currentUserProfile(requestBuilder.build()).get())
            .get();
    Result result = tiController.addApplicant(trustedIntermediaryGroup.id, requestBuilder.build());
    assertThat(result.status()).isEqualTo(SEE_OTHER);
    Optional<ApplicantModel> testApplicant =
        repo.lookupApplicantByEmail("sample2@fake.com").toCompletableFuture().join();
    assertThat(testApplicant.get().getApplicantData().getDateOfBirth().get().toString())
        .isEqualTo("2022-07-18");
    AccountModel account = repo.lookupAccountByEmail("sample2@fake.com").get();
    Http.RequestBuilder requestBuilder2 =
        addCSRFToken(
            fakeRequest()
                .bodyForm(
                    ImmutableMap.of(
                        "firstName",
                        "clientFirst",
                        "middleName",
                        "clientMiddle",
                        "lastName",
                        "clientLast",
                        "dob",
                        "2022-07-07",
                        "emailAddress",
                        "emailControllerSam",
                        "tiNote",
                        "unitTest",
                        "phoneNumber",
                        "4259879090")));
    Http.Request request2 = requestBuilder2.build();
    Result result2 = tiController.updateClientInfo(account.id, request2);

    assertThat(result2.status()).isEqualTo(SEE_OTHER);
    assertThat(repo.lookupAccountByEmail("sample2@fake.com")).isEmpty();
    // assert email address
    assertThat(repo.lookupAccountByEmail("emailControllerSam")).isNotEmpty();
    AccountModel accountFinal = repo.lookupAccountByEmail("emailControllerSam").get();
    // assert ti notes
    assertThat(accountFinal.getTiNote()).isEqualTo("unitTest");

    ApplicantModel applicantModel = accountFinal.newestApplicant().get();
    // assert dob,name,phone
    assertThat(applicantModel.getApplicantData().getDateOfBirth().get().toString())
        .isEqualTo("2022-07-07");
    assertThat(applicantModel.getApplicantData().getApplicantFirstName().get())
        .isEqualTo("clientFirst");
    assertThat(applicantModel.getApplicantData().getApplicantMiddleName().get())
        .isEqualTo("clientMiddle");
    assertThat(applicantModel.getApplicantData().getApplicantLastName().get())
        .isEqualTo("clientLast");
    assertThat(applicantModel.getApplicantData().getPhoneNumber().get()).isEqualTo("4259879090");
  }

  @Test
  public void addApplicant_WithMissingDob() {
    Http.RequestBuilder requestBuilder =
        addCSRFToken(
            Helpers.fakeRequest()
                .bodyForm(
                    ImmutableMap.of(
                        "firstName",
                        "first",
                        "middleName",
                        "middle",
                        "lastName",
                        "last",
                        "emailAddress",
                        "sample1@fake.com",
                        "dob",
                        "")));
    Http.Request request = requestBuilder.build();
    TrustedIntermediaryGroupModel trustedIntermediaryGroup =
        repo.getTrustedIntermediaryGroup(profileUtils.currentUserProfile(request).get()).get();
    Result result = tiController.addApplicant(trustedIntermediaryGroup.id, request);
    assertThat(result.status()).isEqualTo(SEE_OTHER);
    assertThat(result.flash().get("error").get().trim()).isEqualTo("Date of Birth required");
  }

  @Test
  public void addApplicant_WithAllInformation() {
    Http.RequestBuilder requestBuilder =
        addCSRFToken(
            Helpers.fakeRequest()
                .bodyForm(
                    ImmutableMap.of(
                        "firstName",
                        "first",
                        "middleName",
                        "middle",
                        "lastName",
                        "last",
                        "emailAddress",
                        "sample2@fake.com",
                        "dob",
                        "2022-07-18")));

    TrustedIntermediaryGroupModel trustedIntermediaryGroup =
        repo.getTrustedIntermediaryGroup(
                profileUtils.currentUserProfile(requestBuilder.build()).get())
            .get();
    Result result = tiController.addApplicant(trustedIntermediaryGroup.id, requestBuilder.build());
    assertThat(result.status()).isEqualTo(SEE_OTHER);
    Optional<ApplicantModel> testApplicant =
        repo.lookupApplicantByEmail("sample2@fake.com").toCompletableFuture().join();
    assertThat(testApplicant.get().getApplicantData().getDateOfBirth().get().toString())
        .isEqualTo("2022-07-18");
  }
}
