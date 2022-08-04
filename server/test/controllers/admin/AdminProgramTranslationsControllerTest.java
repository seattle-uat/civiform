package controllers.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import models.Program;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import repository.ProgramRepository;
import repository.ResetPostgres;
import services.LocalizedStrings;
import services.program.ProgramDefinition;
import services.program.ProgramNotFoundException;
import support.ProgramBuilder;

public class AdminProgramTranslationsControllerTest extends ResetPostgres {

  private ProgramRepository programRepository;
  private AdminProgramTranslationsController controller;

  @Before
  public void setup() {
    programRepository = instanceOf(ProgramRepository.class);
    controller = instanceOf(AdminProgramTranslationsController.class);
  }

  @Test
  public void edit_rendersFormWithExistingNameAndDescription() throws ProgramNotFoundException {
    Program program = ProgramBuilder.newDraftProgram("test name", "test description").build();

    Result result = controller.edit(addCSRFToken(fakeRequest()).build(), program.id, "en-US");

    assertThat(result.status()).isEqualTo(OK);
    assertThat(contentAsString(result))
        .contains("English", "Spanish", "test name", "test description");
  }

  @Test
  public void edit_programNotFound_returnsNotFound() {
    assertThatThrownBy(() -> controller.edit(addCSRFToken(fakeRequest()).build(), 1000L, "en-US"))
        .isInstanceOf(ProgramNotFoundException.class);
  }

  @Test
  public void update_savesNewFields() throws Exception {
    Program program = ProgramBuilder.newDraftProgram().build();

    Http.RequestBuilder requestBuilder =
        fakeRequest()
            .bodyForm(
                ImmutableMap.of(
                    "displayName", "nombre nuevo", "displayDescription", "este es un programa"));

    Result result = controller.update(addCSRFToken(requestBuilder).build(), program.id, "es-US");

    assertThat(result.status()).isEqualTo(SEE_OTHER);

    ProgramDefinition updatedProgram =
        programRepository
            .lookupProgram(program.id)
            .toCompletableFuture()
            .join()
            .get()
            .getProgramDefinition();
    assertThat(updatedProgram.localizedName().get(Locale.forLanguageTag("es-US")))
        .isEqualTo("nombre nuevo");
    assertThat(updatedProgram.localizedDescription().get(Locale.forLanguageTag("es-US")))
        .isEqualTo("este es un programa");
  }

  @Test
  public void update_programNotFound() {
    assertThatThrownBy(() -> controller.update(addCSRFToken(fakeRequest()).build(), 1000L, "en-US"))
        .isInstanceOf(ProgramNotFoundException.class);
  }

  @Test
  public void update_validationErrors_rendersEditFormWithMessages()
      throws ProgramNotFoundException {
    Program initialProgram =
        ProgramBuilder.newDraftProgram().withName("Internal program name").build();
    // ProgamBuilder initialized the localized name and doesn't currently support
    // providing an addition value for withLocalizedName. Manually update in the
    // case where we want them to diverge.
    Program program =
        initialProgram.getProgramDefinition().toBuilder()
            .setLocalizedName(LocalizedStrings.withDefaultValue("External program name"))
            .build()
            .toProgram();
    program.update();

    Http.RequestBuilder requestBuilder =
        fakeRequest().bodyForm(ImmutableMap.of("displayName", "", "displayDescription", ""));

    Result result = controller.update(addCSRFToken(requestBuilder).build(), program.id, "es-US");

    assertThat(result.status()).isEqualTo(OK);
    assertThat(contentAsString(result))
        .contains(
            String.format("Manage program translations: Internal program name"),
            "program display name cannot be blank",
            "program display description cannot be blank");
  }
}
