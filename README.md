# universal-application-tool

The Universal Application Tool (UAT) aims to simplify the application process for benefits programs by re-using applicant data for multiple benefits applications. It is being developed by Google.org in partnership with the City of Seattle.

## Running a local server

To build the container that runs the app, type `docker build -t uat .`

To run the application, run `docker run -it -p 9000:9000 -v ${PWD}/universal-application-tool-0.0.1:/code uat sbt ~run`
This enables hot-reloading - when you modify files, the server will recompile and restart.
After this, you can access the server at localhost:9000.

To launch postgres and adminer along with play app, run `./bin/run-dev`
This will start 3 individual containers.
You can access play app at localhost:9000 as before, and adminer at localhost:8080.

### Dev database

Whenever a new database container is created, it is empty, i.e. it is not linked to any external storage.
Note if you restart a paused container, you could see the change(s) applied from last session.
You can obtain a fresh container by removing the existing one on docker dashboard.

As the database is configured today, it does not persist data after it terminates, but this can be changed.

If you wish to create new table(s) or change schema, please add the SQL(s) under `conf/evolutions/default`.
You will need to create corresponding EBean model(s) under `app/models`, and potentially a repository under `app/repository` for how you'd like to interact with the table(s).

In dev mode, whenever you first start the app, Play confirms with you if the database is up-to-date and whether you want to apply the evolution scrtips to set up schema or if the database is out-of-sync and needs manual resolution.
If the database is in an inconsistent state, it is usually easier to trash the problem database and start a new one in dev mode.

If we want to undo a schema change, we can create new evolution scripts that change the schema or simply remove existing scripts that create the schema we don't want. If we choose the latter, we likely need to manually reconcile existing database state or we have to start a new one.

## Run tests

To run the tests, run `docker run -it -v ${PWD}/universal-application-tool-0.0.1:/code uat sbt test`
This include all tests under test/

## Development standards

### Client-server concerns

The client should be as simple as is practical to implement the desired user experience.

* Pages should only include the JavaScript needed to implement their own behavior.
* Client-side JavaScript should have no need to manage browser history or derive state from the URL.
* Client-side JavaScript should avoid API-driven interactions, and instead access JSON embedded in the initial page load and submit data using HTML forms.

For example, enable/disable logic in forms can be specified server-side with HTML [data attributes](https://developer.mozilla.org/en-US/docs/Learn/HTML/Howto/Use_data_attributes) then implemented with generic client-side JS that responds to DOM events relevant to the attribute-specified elements. [Here's a simple example](https://jsfiddle.net/c8g6y0ru/1/).

### Java code

Java code should conform to the Google Java [styleguide](https://google.github.io/styleguide/javaguide.html). The project makes use of a linter and autoformatter for Java to help with this.

We have an autoformatter for java code, if there isn't one in your IDE - just run `bin/fmt` and your code should be automatically formatted.

Prefer using immutable collection types provided by [Guava](https://github.com/google/guava) ([API docs](https://guava.dev/releases/snapshot/api/docs/)) over the Java standard library's mutable collections unless impractical. Include a comment justifying the use of a mutable collection if you use one.

#### Separation of concerns

See [wikipedia definition](https://en.wikipedia.org/wiki/Separation_of_concerns).

In lieu of a microservice architecture, this project necessitates special care in ensuring separation of concerns. While Play provides some structure in this regard, it should be viewed as a starting point.

Code in **Play controllers** should be limited to brokering interaction between the server's business logic systems and HTTP. Code in controllers should never directly implement business logic concerns and should instead delegate to classes specific to those purposes. One way to help think about this when writing controller code: if you're writing an HTTP handler that responds with HTML, factor out business logic classes so that implementing another handler that performs the same logic but responds with JSON benefits from high code re-use.

Code in **ebean models** should be limited to brokering interaction between the server's business logic and the database. Code in models should never directly implement business logic concerns.

### Routing and controller methods

APIs should follow [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) when possible with the appropriate HTTP verb. Routing should be [resource-oriented](https://www.oreilly.com/library/view/restful-web-services/9780596529260/ch04.html) ([relevant AIP](https://google.aip.dev/121)). Path names should be [kebab-case](https://en.wikipedia.org/wiki/Letter_case#Special_case_styles).

#### HTML routing convention

For a resource called "programs" that implements the standard actions via HTML requests the routes would be:

|HTTP verb|URL path          |Controller#method         |Use                                                                    |
|---------|------------------|--------------------------|-----------------------------------------------------------------------|
|GET      |/programs         |ProgramsController#index  |Get a list of all programs                                             |
|GET      |/programs/new     |ProgramsController#new    |Get an HTML form for creating a new program                            |
|POST     |/programs         |ProgramsController#create |Create a new program, probably redirect to the #show method to view it |
|GET      |/programs/:id     |ProgramsController#show   |Get the details of a specific program                                  |
|GET      |/programs/:id/edit|ProgramsController#edit   |Get an HTML form for editing an existing program                       |
|PATCH/PUT|/programs/:id     |ProgramsController#update |Update an existing program                                             |
|DELETE   |/programs/:id     |ProgramsController#destroy|Delete an existing program, probably redirect to the #index method     |

#### API routing convention

For the same resource accessed via JSON API the routes should be under the "/api" namespace and naturally do not require form-serving endpoints:

|HTTP verb|URL path              |Controller#method            |Use                                                                    |
|---------|----------------------|-----------------------------|-----------------------------------------------------------------------|
|GET      |/api/programs         |ProgramsApiController#index  |Get a list of all programs                                             |
|POST     |/api/programs         |ProgramsApiController#create |Create a new program                                                   |
|GET      |/api/programs/:id     |ProgramsApiController#show   |Get the details of a specific program                                  |
|PATCH/PUT|/api/programs/:id     |ProgramsApiController#update |Update an existing program                                             |
|DELETE   |/api/programs/:id     |ProgramsApiController#destroy|Delete an existing program                                             |

### Testing

We aim for complete unit test coverage of all execution paths in the system. If you submit code that is infeasible or impractical to get full test coverage for, consider refactoring. If you would like to make an exception, include a clear explanation for why in your PR description.

All major user-facing features should be covered by a functional browser test.
