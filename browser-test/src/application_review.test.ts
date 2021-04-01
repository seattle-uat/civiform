import { startSession, loginAsAdmin, AdminQuestions, endSession } from './support'

describe('normal application flow', () => {
  it('all major steps', async () => {
    const { browser, page } = await startSession()

    await loginAsAdmin(page)
    const adminQuestions = new AdminQuestions(page)

    await adminQuestions.addAddressQuestion('What is your address?')
    await adminQuestions.addNameQuestion('What is your name?')
    await adminQuestions.addNumberQuestion('Give me a number')
    await adminQuestions.addTextQuestion('What is your favorite color?')

    await endSession(browser)
  })
})
