package controllers.admin;

import static com.google.common.base.Preconditions.checkNotNull;

import forms.ProgramForm;
import javax.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.program.ProgramNotFoundException;
import services.program.ProgramService;
import views.admin.ProgramEditView;
import views.admin.ProgramIndexView;
import views.admin.ProgramNewOneView;

/**
 * This controller contains an action to handle HTTP requests to the UAT's Admin "Create
 * application" page.
 */
public class AdminProgramController extends Controller {

  private final ProgramService service;
  private final ProgramIndexView listView;
  private final ProgramNewOneView newOneView;
  private final ProgramEditView editView;
  private final FormFactory formFactory;

  @Inject
  public AdminProgramController(
      ProgramService service,
      ProgramIndexView listView,
      ProgramNewOneView newOneView,
      ProgramEditView editView,
      FormFactory formFactory) {
    this.service = checkNotNull(service);
    this.listView = checkNotNull(listView);
    this.newOneView = checkNotNull(newOneView);
    this.editView = checkNotNull(editView);
    this.formFactory = checkNotNull(formFactory);
  }

  public Result index() {
    return ok(listView.render(this.service.listProgramDefinitions()));
  }

  public Result newOne(Request request) {
    return ok(newOneView.render(request));
  }

  public Result create(Request request) {
    Form<ProgramForm> programForm = formFactory.form(ProgramForm.class);
    ProgramForm program = programForm.bindFromRequest(request).get();
    service.createProgramDefinition(program.getName(), program.getDescription());
    return found(routes.AdminProgramController.index());
  }

  public Result edit(Request request, long id) throws ProgramNotFoundException {
    return ok(
        editView.render(
            request,
            service.getProgramDefinition(id).orElseThrow(() -> new ProgramNotFoundException(id))));
  }

  public Result update(Request request, long id) throws ProgramNotFoundException {
    Form<ProgramForm> programForm = formFactory.form(ProgramForm.class);
    ProgramForm program = programForm.bindFromRequest(request).get();
    service.updateProgramDefinition(id, program.getName(), program.getDescription());
    return found(routes.AdminProgramController.index());
  }
}
