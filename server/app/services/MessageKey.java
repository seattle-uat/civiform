package services;

/** Contains keys into the {@code messages} files used for translation. */
public enum MessageKey {
  ADDRESS_CORRECTION_VERIFY_TITLE("title.verifyAddress"),
  ADDRESS_CORRECTION_NO_VALID_TITLE("title.noValidAddress"),
  ADDRESS_CORRECTION_VERIFY_INSTRUCTIONS("content.foundSimilarAddress"),
  ADDRESS_CORRECTION_NO_VALID_INSTRUCTIONS("content.noValidAddress"),
  ADDRESS_CORRECTION_AS_ENTERED_HEADING("content.addressEntered"),
  ADDRESS_CORRECTION_SUGGESTED_ADDRESS_HEADING("content.suggestedAddress"),
  ADDRESS_CORRECTION_SUGGESTED_ADDRESSES_HEADING("content.suggestedAddresses"),
  ADDRESS_LABEL_CITY("label.city"),
  ADDRESS_LABEL_LINE_2("label.addressLine2"),
  ADDRESS_LABEL_STATE("label.state"),
  ADDRESS_LABEL_STATE_SELECT("label.selectState"),
  ADDRESS_LABEL_STREET("label.street"),
  ADDRESS_LABEL_ZIPCODE("label.zipcode"),
  ADDRESS_VALIDATION_CITY_REQUIRED("validation.cityRequired"),
  ADDRESS_VALIDATION_INVALID_ZIPCODE("validation.invalidZipcode"),
  ADDRESS_VALIDATION_NO_PO_BOX("validation.noPoBox"),
  ADDRESS_VALIDATION_STATE_REQUIRED("validation.stateRequired"),
  ADDRESS_VALIDATION_STREET_REQUIRED("validation.streetRequired"),
  ARIA_LABEL_EDIT("ariaLabel.edit"),
  ARIA_LABEL_ANSWER("ariaLabel.answer"),
  BANNER_ERROR_SAVING_APPLICATION("banner.errorSavingApplication"),
  BUTTON_APPLY("button.apply"),
  BUTTON_APPLY_TO_PROGRAMS("button.applyToPrograms"),
  BUTTON_APPLY_SR("button.applySr"),
  BUTTON_CHOOSE_FILE("button.chooseFile"),
  BUTTON_CONTINUE("button.continue"),
  BUTTON_CONTINUE_SR("button.continueSr"),
  BUTTON_CONTINUE_COMMON_INTAKE_SR("button.continueCommonIntakeSr"),
  BUTTON_CONTINUE_WITHOUT_AN_ACCOUNT("button.continueWithoutAnAccount"),
  BUTTON_CREATE_ACCOUNT("button.createAccount"),
  BUTTON_EDIT("button.edit"),
  BUTTON_EDIT_SR("button.editSr"),
  BUTTON_EDIT_COMMON_INTAKE_SR("button.editCommonIntakeSr"),
  BUTTON_GO_BACK_AND_EDIT("button.goBackAndEdit"),
  BUTTON_LOGIN("button.login"),
  BUTTON_CREATE_AN_ACCOUNT("button.createAnAccount"),
  BUTTON_LOGIN_GUEST("button.guestLogin"),
  BUTTON_LOGOUT("button.logout"),
  BUTTON_NEXT_SCREEN("button.nextScreen"),
  BUTTON_PREVIOUS_SCREEN("button.previousScreen"),
  BUTTON_REVIEW("button.review"),
  BUTTON_DELETE_FILE("button.deleteFile"),
  BUTTON_KEEP_FILE("button.keepFile"),
  BUTTON_SKIP_FILEUPLOAD("button.skipFileUpload"),
  BUTTON_START_HERE("button.startHere"),
  BUTTON_CONTINUE_TO_APPLICATION("button.continueToApplication"),
  BUTTON_START_HERE_COMMON_INTAKE_SR("button.startHereCommonIntakeSr"),
  BUTTON_SUBMIT("button.submit"),
  BUTTON_UNTRANSLATED_SUBMIT("button.untranslatedSubmit"),
  CURRENCY_VALIDATION_MISFORMATTED("validation.currencyMisformatted"),
  CONTENT_ADMIN_LOGIN_PROMPT("content.adminLoginPrompt"),
  CONTENT_ADMIN_FOOTER_PROMPT("content.adminFooterPrompt"),
  CONTENT_SAVE_TIME("content.saveTime"),
  CONTENT_CHANGE_ELIGIBILITY_ANSWERS("content.changeAnswersForEligibility"),
  CONTENT_CIVIFORM_DESCRIPTION("content.description"),
  CONTENT_CONFIRMED("content.confirmed"),
  CONTENT_DOES_NOT_QUALIFY("content.doesNotQualify"),
  CONTENT_COMMON_INTAKE_CONFIRMATION("content.commonIntakeConfirmation"),
  CONTENT_COMMON_INTAKE_CONFIRMATION_TI("content.commonIntakeConfirmationTi"),
  CONTENT_COMMON_INTAKE_NO_MATCHING_PROGRAMS("content.commonIntakeNoMatchingPrograms"),
  CONTENT_COMMON_INTAKE_NO_MATCHING_PROGRAMS_TI("content.commonIntakeNoMatchingProgramsTi"),
  CONTENT_COMMON_INTAKE_NO_MATCHING_PROGRAMS_NEXT_STEP(
      "content.commonIntakeNoMatchingProgramsNextStep"),
  CONTENT_ELIGIBILITY_CRITERIA("content.eligibilityCriteria"),
  CONTENT_GET_BENEFITS("content.benefits"),
  CONTENT_GUEST_DESCRIPTION("content.guestDescription"),
  CONTENT_LOGIN_PROMPT("content.loginPrompt"),
  CONTENT_LOGIN_DISABLED_PROMPT("content.loginDisabledPrompt"),
  CONTENT_LOGIN_PROMPT_ALTERNATIVE("content.alternativeLoginPrompt"),
  CONTENT_OR("content.or"),
  CONTENT_PLEASE_CREATE_ACCOUNT("content.pleaseCreateAccount"),
  CONTENT_PREVIOUSLY_ANSWERED_ON("content.previouslyAnsweredOn"),
  CONTENT_SELECT_LANGUAGE("label.selectLanguage"),
  ERROR_ANNOUNCEMENT_SR("validation.errorAnnouncementSr"),
  ERROR_NOT_FOUND_TITLE("error.notFoundTitle"),
  ERROR_NOT_FOUND_DESCRIPTION("error.notFoundDescription"),
  ERROR_NOT_FOUND_DESCRIPTION_LINK("error.notFoundDescriptionLink"),
  DATE_VALIDATION_MISFORMATTED("validation.dateMisformatted"),
  DROPDOWN_PLACEHOLDER("placeholder.noDropdownSelection"),
  END_SESSION("header.endSession"),
  EMAIL_APPLICATION_RECEIVED_BODY("email.applicationReceivedBody"),
  EMAIL_APPLICATION_RECEIVED_SUBJECT("email.applicationReceivedSubject"),
  EMAIL_APPLICATION_UPDATE_SUBJECT("email.applicationUpdateSubject"),
  EMAIL_LOGIN_TO_CIVIFORM("email.loginToCiviform"),
  EMAIL_TI_APPLICATION_SUBMITTED_BODY("email.tiApplicationSubmittedBody"),
  EMAIL_TI_APPLICATION_SUBMITTED_SUBJECT("email.tiApplicationSubmittedSubject"),
  EMAIL_TI_APPLICATION_UPDATE_SUBJECT("email.tiApplicationUpdateSubject"),
  EMAIL_TI_APPLICATION_UPDATE_BODY("email.tiApplicationUpdateBody"),
  EMAIL_TI_MANAGE_YOUR_CLIENTS("email.tiManageYourClients"),
  ENUMERATOR_BUTTON_ADD_ENTITY("button.addEntity"),
  ENUMERATOR_BUTTON_REMOVE_ENTITY("button.removeEntity"),
  ENUMERATOR_DIALOG_CONFIRM_DELETE("dialog.confirmDelete"),
  ENUMERATOR_PLACEHOLDER_ENTITY_NAME("placeholder.entityName"),
  ENUMERATOR_VALIDATION_DUPLICATE_ENTITY_NAME("validation.duplicateEntityName"),
  ENUMERATOR_VALIDATION_ENTITY_REQUIRED("validation.entityNameRequired"),
  FILEUPLOAD_VALIDATION_FILE_REQUIRED("validation.fileRequired"),
  FOOTER_SUPPORT_LINK_DESCRIPTION("footer.supportLinkDescription"),
  GENERAL_LOGIN_MODAL_PROMPT("content.generalLoginModalPrompt"),
  GUEST("guest"),
  GUEST_INDICATOR("header.guestIndicator"),
  ID_VALIDATION_NUMBER_REQUIRED("validation.numberRequired"),
  ID_VALIDATION_TOO_LONG("validation.idTooLong"),
  ID_VALIDATION_TOO_SHORT("validation.idTooShort"),
  INITIAL_LOGIN_MODAL_PROMPT("content.initialLoginModalPrompt"),
  INPUT_FILE_ALREADY_UPLOADED("input.fileAlreadyUploaded"),
  INVALID_INPUT("validation.invalidInput"),
  LANGUAGE_LABEL_SR("label.languageSr"),
  LINK_ADMIN_LOGIN("link.adminLogin"),
  LINK_ALL_DONE("link.allDone"),
  LINK_APPLY_TO_ANOTHER_PROGRAM("link.applyToAnotherProgram"),
  LINK_CREATE_ACCOUNT_OR_SIGN_IN("link.createAccountOrSignIn"),
  LINK_EDIT("link.edit"),
  LINK_ANSWER("link.answer"),
  LINK_PROGRAM_DETAILS("link.programDetails"),
  LINK_PROGRAM_DETAILS_SR("link.programDetailsSr"),
  MOBILE_FILE_UPLOAD_HELP("content.mobileFileUploadHelp"),
  MULTI_OPTION_VALIDATION("adminValidation.multiOptionEmpty"),
  MULTI_SELECT_VALIDATION_TOO_FEW("validation.tooFewSelections"),
  MULTI_SELECT_VALIDATION_TOO_MANY("validation.tooManySelections"),
  NAME_LABEL_FIRST("label.firstName"),
  NAME_LABEL_LAST("label.lastName"),
  NAME_LABEL_MIDDLE("label.middleName"),
  NAME_PLACEHOLDER_FIRST("placeholder.firstName"),
  NAME_PLACEHOLDER_LAST("placeholder.lastName"),
  NAME_PLACEHOLDER_MIDDLE("placeholder.middleName"),
  NAME_VALIDATION_FIRST_REQUIRED("validation.firstNameRequired"),
  NAME_VALIDATION_LAST_REQUIRED("validation.lastNameRequired"),
  PHONE_VALIDATION_NUMBER_REQUIRED("validation.phoneNumberRequired"),
  PHONE_VALIDATION_COUNTRY_CODE_REQUIRED("validation.phoneCountryCodeRequired"),
  PHONE_VALIDATION_NON_NUMBER_VALUE("validation.phoneNumberMustContainNumbersOnly"),
  PHONE_VALIDATION_INVALID_PHONE_NUMBER("validation.invalidPhoneNumberProvided"),
  PHONE_VALIDATION_NUMBER_NOT_IN_COUNTRY("validation.phoneMustBeLocalToCountry"),
  PHONE_LABEL_COUNTRY_CODE("label.countryCode"),
  PHONE_LABEL_PHONE_NUMBER("label.phoneNumber"),
  NUMBER_VALIDATION_TOO_BIG("validation.numberTooBig"),
  NUMBER_VALIDATION_TOO_SMALL("validation.numberTooSmall"),
  NUMBER_VALIDATION_NON_INTEGER("validation.numberNonInteger"),
  REQUIRED_FIELDS_ANNOTATION("content.requiredFieldsAnnotation"),
  SUBMITTED_DATE("content.submittedDate"),
  TAG_MAY_NOT_QUALIFY("tag.mayNotQualify"),
  TAG_MAY_NOT_QUALIFY_TI("tag.mayNotQualifyTi"),
  TAG_MAY_QUALIFY("tag.mayQualify"),
  TAG_MAY_QUALIFY_TI("tag.mayQualifyTi"),
  TEXT_VALIDATION_TOO_LONG("validation.textTooLong"),
  TEXT_VALIDATION_TOO_SHORT("validation.textTooShort"),
  TITLE_ALL_PROGRAMS_SECTION("title.allProgramsSection"),
  TITLE_APPLICATION_CONFIRMATION("title.applicationConfirmation"),
  TITLE_COMMON_INTAKE_CONFIRMATION("title.commonIntakeConfirmation"),
  TITLE_COMMON_INTAKE_CONFIRMATION_TI("title.commonIntakeConfirmationTi"),
  TITLE_APPLICATION_NOT_ELIGIBLE("title.applicantNotEligible"),
  TITLE_APPLICATION_NOT_ELIGIBLE_TI("title.applicantNotEligibleTi"),
  TITLE_COMMON_INTAKE_SUMMARY("title.commonIntakeSummary"),
  TITLE_CREATE_AN_ACCOUNT("title.createAnAccount"),
  TITLE_FIND_SERVICES_SECTION("title.findServicesSection"),
  TITLE_LOGIN("title.login"),
  TITLE_PROGRAMS("title.programs"),
  TITLE_PROGRAMS_ACTIVE_UPDATED("title.activeProgramsUpdated"),
  TITLE_PROGRAMS_IN_PROGRESS_UPDATED("title.inProgressProgramsUpdated"),
  TITLE_PROGRAM_SUMMARY("title.programSummary"),
  TITLE_PROGRAMS_SUBMITTED("title.submittedPrograms"),
  TITLE_STATUS("title.status"),
  TOAST_APPLICATION_SAVED("toast.applicationSaved"),
  TOAST_APPLICATION_OUT_OF_DATE("toast.applicationOutOfDate"),
  TOAST_LOCALE_NOT_SUPPORTED("toast.localeNotSupported"),
  TOAST_MAY_NOT_QUALIFY("toast.mayNotQualify"),
  TOAST_MAY_NOT_QUALIFY_TI("toast.mayNotQualifyTi"),
  TOAST_MAY_QUALIFY("toast.mayQualify"),
  TOAST_MAY_QUALIFY_TI("toast.mayQualifyTi"),
  TOAST_PROGRAM_COMPLETED("toast.programCompleted"),
  TOAST_SESSION_ENDED("toast.sessionEnded"),
  USER_NAME("header.userName"),
  VALIDATION_REQUIRED("validation.isRequired");

  private final String keyName;

  MessageKey(String keyName) {
    this.keyName = keyName;
  }

  public String getKeyName() {
    return this.keyName;
  }
}
