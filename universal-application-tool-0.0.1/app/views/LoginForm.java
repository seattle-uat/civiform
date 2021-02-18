package views;

import static j2html.TagCreator.body;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;

import akka.parboiled2.RuleTrace;
import com.google.inject.Inject;
import play.mvc.Http;
import play.twirl.api.Content;

public class LoginForm extends BaseHtmlView {
  private final BaseHtmlLayout layout;

  @Inject
  public LoginForm(BaseHtmlLayout layout) {
    this.layout = layout;
  }

  public Content render(Http.Request request) {
    return layout.htmlContent(
        body(
            h1("Log In"),
            form(
                    makeCsrfTokenInputTag(request),
                    textField("username", "Username"),
                    passwordField("password", "Password"),
                    submitButton("Submit"))
                .withMethod("POST")
                .withAction("/callback?client_name=FormClient")));
  }
}
