import {
  AdminPrograms,
  AdminQuestions,
  ApplicantQuestions,
  createBrowserContext,
  loginAsAdmin,
  loginAsGuest,
  logout,
  selectApplicantLanguage,
  validateAccessibility,
  validateScreenshot,
} from './support'

describe('Text question for applicant flow', () => {
  const ctx = createBrowserContext(/* clearDb= */ false)

  describe('single text question', () => {
    let applicantQuestions: ApplicantQuestions
    const programName = 'test program for single text q'

    beforeAll(async () => {
      // As admin, create program with a free form text question.
      await loginAsAdmin(ctx.page)
      const adminQuestions = new AdminQuestions(ctx.page)
      const adminPrograms = new AdminPrograms(ctx.page)
      applicantQuestions = new ApplicantQuestions(ctx.page)

      await adminQuestions.addTextQuestion({
        questionName: 'text-q',
        minNum: 5,
        maxNum: 20,
      })
      await adminPrograms.addAndPublishProgramWithQuestions(
        ['text-q'],
        programName,
      )

      await logout(ctx.page)
    })

    it('validate screenshot', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)

      await validateScreenshot(ctx.page, 'text')
    })

    it('validate screenshot with errors', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.clickNext()

      await validateScreenshot(ctx.page, 'text-errors')
    })

    it('with text submits successfully', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.answerTextQuestion('I love CiviForm!')
      await applicantQuestions.clickNext()

      await applicantQuestions.submitFromReviewPage()
    })

    it('with empty text does not submit', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)

      // Click next without inputting anything
      await applicantQuestions.clickNext()

      const textId = '.cf-question-text'
      expect(await ctx.page.innerText(textId)).toContain(
        'This question is required.',
      )
    })

    it('with too short text does not submit', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.answerTextQuestion('hi')
      await applicantQuestions.clickNext()

      const textId = '.cf-question-text'
      expect(await ctx.page.innerText(textId)).toContain(
        'Must contain at least 5 characters.',
      )
    })

    it('with too long text does not submit', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.answerTextQuestion(
        'A long string that exceeds the character limit',
      )
      await applicantQuestions.clickNext()

      const textId = '.cf-question-text'
      expect(await ctx.page.innerText(textId)).toContain(
        'Must contain at most 20 characters.',
      )
    })
  })

  describe('multiple text questions', () => {
    let applicantQuestions: ApplicantQuestions
    const programName = 'test program for multiple text qs'

    beforeAll(async () => {
      await loginAsAdmin(ctx.page)
      const adminQuestions = new AdminQuestions(ctx.page)
      const adminPrograms = new AdminPrograms(ctx.page)
      applicantQuestions = new ApplicantQuestions(ctx.page)

      await adminQuestions.addTextQuestion({
        questionName: 'first-text-q',
        minNum: 5,
        maxNum: 20,
      })
      await adminQuestions.addTextQuestion({
        questionName: 'second-text-q',
        minNum: 5,
        maxNum: 20,
      })

      await adminPrograms.addProgram(programName)
      await adminPrograms.editProgramBlockWithOptional(
        programName,
        'Optional question block',
        ['second-text-q'],
        'first-text-q', // optional
      )
      await adminPrograms.gotoAdminProgramsPage()
      await adminPrograms.publishAllPrograms()

      await logout(ctx.page)
    })

    it('with both selections submits successfully', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.answerTextQuestion('I love CiviForm!', 0)
      await applicantQuestions.answerTextQuestion('You love CiviForm!', 1)
      await applicantQuestions.clickNext()

      await applicantQuestions.submitFromReviewPage()
    })

    it('with unanswered optional question submits successfully', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      // Only answer second question. First is optional.
      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.answerTextQuestion('You love CiviForm!', 1)
      await applicantQuestions.clickNext()

      await applicantQuestions.submitFromReviewPage()
    })

    it('with first invalid does not submit', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.answerTextQuestion(
        'A long string that exceeds the character limit',
        0,
      )
      await applicantQuestions.answerTextQuestion('You love CiviForm!', 1)
      await applicantQuestions.clickNext()

      const textId = '.cf-question-text'
      expect(await ctx.page.innerText(textId)).toContain(
        'Must contain at most 20 characters.',
      )
    })

    it('with second invalid does not submit', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)
      await applicantQuestions.answerTextQuestion('I love CiviForm!', 0)
      await applicantQuestions.answerTextQuestion(
        'A long string that exceeds the character limit',
        1,
      )
      await applicantQuestions.clickNext()

      const textId = `.cf-question-text >> nth=1`
      expect(await ctx.page.innerText(textId)).toContain(
        'Must contain at most 20 characters.',
      )
    })

    it('has no accessiblity violations', async () => {
      await loginAsGuest(ctx.page)
      await selectApplicantLanguage(ctx.page, 'English')

      await applicantQuestions.applyProgram(programName)

      await validateAccessibility(ctx.page)
    })
  })
})
