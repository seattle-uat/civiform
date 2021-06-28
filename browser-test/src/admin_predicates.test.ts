import { AdminPredicates, AdminPrograms, AdminQuestions, ApplicantQuestions, endSession, loginAsAdmin, loginAsProgramAdmin, loginAsTestUser, logout, selectApplicantLanguage, startSession, userDisplayName } from './support'

describe('create and edit predicates', () => {
  it('add a hide predicate', async () => {
    const { browser, page } = await startSession();

    await loginAsAdmin(page);
    const adminQuestions = new AdminQuestions(page);
    const adminPrograms = new AdminPrograms(page);

    // Add a program with two screens
    await adminQuestions.addTextQuestion('hide-predicate-q');
    await adminQuestions.addTextQuestion('hide-other-q', 'desc', 'conditional question');

    const programName = 'create hide predicate';
    await adminPrograms.addProgram(programName);
    await adminPrograms.editProgramBlock(programName, 'first screen', ['hide-predicate-q']);
    await adminPrograms.addProgramBlock(programName, 'screen with predicate', ['hide-other-q']);

    // Edit predicate for second block
    await adminPrograms.goToEditBlockPredicatePage(programName, 'Screen 2');
    const adminPredicates = new AdminPredicates(page);
    await adminPredicates.addPredicate('hide-predicate-q', 'hidden if', 'text', 'is equal to', 'hide me');
    await adminPredicates.expectVisibilityConditionEquals('Screen 2 is hidden if hide-predicate-q\'s text is equal to "hide me"');

    // Publish the program
    await adminPrograms.publishProgram(programName);

    // Switch to the applicant view and apply to the program
    await logout(page);
    await loginAsTestUser(page);
    await selectApplicantLanguage(page, 'English');
    const applicant = new ApplicantQuestions(page);
    await applicant.applyProgram(programName);

    // Initially fill out the first screen so that the next screen will be shown
    await applicant.answerTextQuestion('show me');
    await applicant.clickNext();

    // Fill out the second screen
    await applicant.answerTextQuestion('will be hidden and not submitted');
    await applicant.clickNext();

    // We should be on the review page, with an answer to Screen 2's question
    expect(await page.innerText('#application-summary')).toContain('conditional question');

    // Return to the first screen and answer it so that the second screen is hidden
    page.click('text=Edit'); // first screen edit
    await applicant.answerTextQuestion('hide me');
    await applicant.clickNext();

    // We should be on the review page
    expect(await page.innerText('#application-summary')).not.toContain('conditional question');
    await applicant.submitFromReviewPage(programName);

    // Visit the program admin page and assert the hidden question does not show
    await logout(page);
    await loginAsProgramAdmin(page);
    await adminPrograms.viewApplications(programName);
    await adminPrograms.viewApplicationForApplicant(userDisplayName());
    expect(await page.innerText('#application-view')).not.toContain('Screen 2');

    await endSession(browser);
  });

  it('add a show predicate', async () => {
    const { browser, page } = await startSession();

    await loginAsAdmin(page);
    const adminQuestions = new AdminQuestions(page);
    const adminPrograms = new AdminPrograms(page);

    // Add a program with two screens
    await adminQuestions.addTextQuestion('show-predicate-q');
    await adminQuestions.addTextQuestion('show-other-q', 'desc', 'conditional question');

    const programName = 'create show predicate';
    await adminPrograms.addProgram(programName);
    await adminPrograms.editProgramBlock(programName, 'first screen', ['show-predicate-q']);
    await adminPrograms.addProgramBlock(programName, 'screen with predicate', ['show-other-q']);

    // Edit predicate for second screen
    await adminPrograms.goToEditBlockPredicatePage(programName, 'Screen 2');
    const adminPredicates = new AdminPredicates(page);
    await adminPredicates.addPredicate('show-predicate-q', 'shown if', 'text', 'is equal to', 'show me');
    await adminPredicates.expectVisibilityConditionEquals('Screen 2 is shown if show-predicate-q\'s text is equal to "show me"');

    // Publish the program
    await adminPrograms.publishProgram(programName);

    // Switch to the applicant view and apply to the program
    await logout(page);
    await loginAsTestUser(page);
    await selectApplicantLanguage(page, 'English');
    const applicant = new ApplicantQuestions(page);
    await applicant.applyProgram(programName);

    // Initially fill out the first screen so that the next screen will be hidden
    await applicant.answerTextQuestion('hide next screen');
    await applicant.clickNext();

    // We should be on the review page, with no Screen 2 questions shown. We should
    // be able to submit the application
    expect(await page.innerText('#application-summary')).not.toContain('conditional question');
    expect((await page.innerText('.cf-submit-button')).toLowerCase()).toContain('submit');

    // Return to the first screen and answer it so that the second screen is shown
    page.click('text=Edit'); // first screen edit
    await applicant.answerTextQuestion('show me');
    await applicant.clickNext();

    // The second screen should now appear, and we must fill it out
    await applicant.answerTextQuestion('hello world!');
    await applicant.clickNext();

    // We should be on the review page
    expect(await page.innerText('#application-summary')).toContain('conditional question');
    await applicant.submitFromReviewPage(programName);

    // Visit the program admin page and assert the hidden question does not show
    await logout(page);
    await loginAsProgramAdmin(page);
    await adminPrograms.viewApplications(programName);
    await adminPrograms.viewApplicationForApplicant(userDisplayName());
    expect(await page.innerText('#application-view')).toContain('Screen 2');

    await endSession(browser);
  })
})
