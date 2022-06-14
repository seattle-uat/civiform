package views.components;

import services.question.types.QuestionType;

/**
 * Class to hold constants for icons and provide methods for rendering SVG components. Most of these
 * icons are from https://fonts.google.com/icons, each one is commented with its icon name.
 */
public class Icons {

  public static final String ADD_SVG_PATH =
      "M9.125 15.833V10.875H4.167V9.125H9.125V4.167H10.875V9.125H15.833V10.875H10.875V15.833Z";
  // Place
  public static final String ADDRESS_SVG_PATH =
      "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38"
          + " 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z";
  public static final String ANNOTATION_SVG_PATH =
      "M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z";
  // Check Box
  public static final String CHECKBOX_SVG_PATH =
      "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0"
          + " 16H5V5h14v14zM17.99 9l-1.41-1.42-6.59 6.59-2.58-2.57-1.42 1.41 4 3.99z";
  // Payments
  public static final String CURRENCY_SVG_PATH =
      "M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3"
          + " 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11"
          + " 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z";
  public static final String DATE_SVG_PATH =
      "M19 4h-1V3c0-.55-.45-1-1-1s-1 .45-1 1v1H8V3c0-.55-.45-1-1-1s-1 .45-1 1v1H5c-1.11"
          + " 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 15c0"
          + " .55-.45 1-1 1H6c-.55 0-1-.45-1-1V9h14v10zM7 11h2v2H7zm4 0h2v2h-2zm4 0h2v2h-2z";
  public static final String DOWNLOAD_SVG_PATH =
      "M10 13.271 5.708 8.979 6.958 7.729 9.125 9.896V3.333H10.875V9.896L13.042 7.729L14.292"
          + " 8.979ZM5.083 16.667Q4.354 16.667 3.844 16.156Q3.333 15.646 3.333"
          + " 14.917V12.5H5.083V14.917Q5.083 14.917 5.083 14.917Q5.083 14.917 5.083"
          + " 14.917H14.917Q14.917 14.917 14.917 14.917Q14.917 14.917 14.917"
          + " 14.917V12.5H16.667V14.917Q16.667 15.646 16.156 16.156Q15.646 16.667 14.917 16.667Z";
  // Arrow Drop Down Circle
  public static final String DROPDOWN_SVG_PATH =
      "M12 4c4.41 0 8 3.59 8 8s-3.59 8-8 8-8-3.59-8-8 3.59-8 8-8m0-2C6.48 2 2 6.48 2 12s4.48 10 10"
          + " 10 10-4.48 10-10S17.52 2 12 2zm0 13l-4-4h8z";
  public static final String EDIT_SVG_PATH =
      "M4.25 15.75H5.479L13.5 7.729L12.896 7.104L12.271 6.5L4.25 14.521ZM2.5 17.5V13.771L13.479"
          + " 2.792Q14 2.271 14.719 2.271Q15.438 2.271 15.958 2.792L17.208 4.042Q17.708 4.542"
          + " 17.708 5.281Q17.708 6.021 17.208 6.521L6.229 17.5ZM15.958 5.271 14.729 4.042ZM13.5"
          + " 7.729 12.896 7.104 12.271 6.5V6.479L13.5 7.729Z";
  // Email
  public static final String EMAIL_SVG_PATH =
      "M22 6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6zm-2 0l-8"
          + " 5-8-5h16zm0 12H4V8l8 5 8-5v10z";
  // Forms Add On
  public static final String ENUMERATOR_SVG_PATH =
      "M19 6H7V4H19ZM7 10V8H19V10ZM7 14V12H13.65Q13.075 12.4 12.613 12.9Q12.15 13.4 11.8 14ZM7"
          + " 16H11.075Q11.025 16.25 11.012 16.488Q11 16.725 11 16.975Q11 17.25 11.025 17.5Q11.05"
          + " 17.75 11.1 18H7ZM16"
          + " 20.975V17.975H13V15.975H16V12.975H18V15.975H21V17.975H18V20.975ZM5 6H3V4H5ZM3"
          + " 10V8H5V10ZM3 14V12H5V14ZM3 16H5V18H3Z";
  // Upload
  public static final String FILEUPLOAD_SVG_PATH =
      "M11 16V7.85L8.4 10.45L7 9L12 4L17 9L15.6 10.45L13 7.85V16ZM6 20Q5.175 20 4.588 19.413Q4"
          + " 18.825 4 18V15H6V18Q6 18 6 18Q6 18 6 18H18Q18 18 18 18Q18 18 18 18V15H20V18Q20"
          + " 18.825 19.413 19.413Q18.825 20 18 20Z";
  public static final String GROUP_SVG_PATH =
      "M0.833 16.667V14.271Q0.833 13.542 1.208 12.958Q1.583 12.375 2.188 12.083Q3.438 11.458 4.812"
          + " 11.146Q6.188 10.833 7.562 10.833Q8.938 10.833 10.312 11.156Q11.688 11.479 12.938"
          + " 12.083Q13.542 12.375 13.917 12.958Q14.292 13.542 14.292 14.271V16.667ZM14.042"
          + " 10.917Q15.146 11.042 16.062 11.344Q16.979 11.646 17.812 12.083Q18.583 12.5 18.875"
          + " 13.021Q19.167 13.542 19.167 14.271V16.667H15.917V14.083Q15.917 13.25 15.448"
          + " 12.385Q14.979 11.521 14.042 10.917ZM7.562 9.979Q6.146 9.979 5.188 9.021Q4.229 8.062"
          + " 4.229 6.646Q4.229 5.229 5.188 4.271Q6.146 3.312 7.562 3.312Q8.979 3.312 9.938"
          + " 4.271Q10.896 5.229 10.896 6.646Q10.896 8.062 9.938 9.021Q8.979 9.979 7.562"
          + " 9.979ZM15.896 6.646Q15.896 8.062 14.938 9.021Q13.979 9.979 12.562 9.979Q12.333 9.979"
          + " 11.927 9.917Q11.521 9.854 11.292 9.792Q11.833 9.125 12.135 8.323Q12.438 7.521 12.438"
          + " 6.646Q12.438 5.792 12.135 4.99Q11.833 4.188 11.292 3.521Q11.604 3.417 11.938"
          + " 3.365Q12.271 3.312 12.562 3.312Q13.979 3.312 14.938 4.271Q15.896 5.229 15.896"
          + " 6.646ZM2.583 14.917H12.542V14.271Q12.542 14.042 12.438 13.885Q12.333 13.729 12.167"
          + " 13.667Q11.083 13.167 9.885 12.875Q8.688 12.583 7.562 12.583Q6.438 12.583 5.24"
          + " 12.865Q4.042 13.146 2.958 13.667Q2.792 13.729 2.688 13.885Q2.583 14.042 2.583"
          + " 14.271ZM7.562 8.229Q8.25 8.229 8.698 7.781Q9.146 7.333 9.146 6.646Q9.146 5.958 8.698"
          + " 5.51Q8.25 5.062 7.562 5.062Q6.875 5.062 6.427 5.51Q5.979 5.958 5.979 6.646Q5.979"
          + " 7.333 6.427 7.781Q6.875 8.229 7.562 8.229ZM7.562 6.646Q7.562 6.646 7.562 6.646Q7.562"
          + " 6.646 7.562 6.646Q7.562 6.646 7.562 6.646Q7.562 6.646 7.562 6.646Q7.562 6.646 7.562"
          + " 6.646Q7.562 6.646 7.562 6.646Q7.562 6.646 7.562 6.646Q7.562 6.646 7.562 6.646ZM7.562"
          + " 12.583Q7.562 12.583 7.562 12.583Q7.562 12.583 7.562 12.583Q7.562 12.583 7.562"
          + " 12.583Q7.562 12.583 7.562 12.583Q7.562 12.583 7.562 12.583Q7.562 12.583 7.562"
          + " 12.583Q7.562 12.583 7.562 12.583Q7.562 12.583 7.562 12.583Z";
  // Badge
  public static final String ID_SVG_PATH =
      "M14 13.5H18V12H14ZM14 16.5H18V15H14ZM15 7H20Q20.825 7 21.413 7.587Q22 8.175 22 9V20Q22"
          + " 20.825 21.413 21.413Q20.825 22 20 22H4Q3.175 22 2.588 21.413Q2 20.825 2 20V9Q2 8.175"
          + " 2.588 7.587Q3.175 7 4 7H9V4Q9 3.175 9.588 2.587Q10.175 2 11 2H13Q13.825 2 14.413"
          + " 2.587Q15 3.175 15 4ZM11 9H13V4H11ZM12 14.5Q12 14.5 12 14.5Q12 14.5 12 14.5Q12 14.5"
          + " 12 14.5Q12 14.5 12 14.5Q12 14.5 12 14.5Q12 14.5 12 14.5Q12 14.5 12 14.5Q12 14.5 12"
          + " 14.5Q12 14.5 12 14.5Q12 14.5 12 14.5Q12 14.5 12 14.5Q12 14.5 12 14.5ZM9 15Q9.625 15"
          + " 10.062 14.562Q10.5 14.125 10.5 13.5Q10.5 12.875 10.062 12.438Q9.625 12 9 12Q8.375 12"
          + " 7.938 12.438Q7.5 12.875 7.5 13.5Q7.5 14.125 7.938 14.562Q8.375 15 9 15ZM6"
          + " 18H12V17.55Q12 17.125 11.762 16.762Q11.525 16.4 11.1 16.2Q10.6 15.975 10.088"
          + " 15.863Q9.575 15.75 9 15.75Q8.425 15.75 7.913 15.863Q7.4 15.975 6.9 16.2Q6.475 16.4"
          + " 6.238 16.762Q6 17.125 6 17.55ZM9 9H4Q4 9 4 9Q4 9 4 9V20Q4 20 4 20Q4 20 4 20H20Q20 20"
          + " 20 20Q20 20 20 20V9Q20 9 20 9Q20 9 20 9H15Q15 9.825 14.413 10.412Q13.825 11 13"
          + " 11H11Q10.175 11 9.588 10.412Q9 9.825 9 9Z";
  public static final String LANGUAGE_SVG_PATH =
      "M10 18.333Q8.292 18.333 6.771 17.677Q5.25 17.021 4.115 15.885Q2.979 14.75 2.323"
          + " 13.229Q1.667 11.708 1.667 10Q1.667 8.271 2.323 6.76Q2.979 5.25 4.115 4.115Q5.25"
          + " 2.979 6.771 2.323Q8.292 1.667 10 1.667Q11.729 1.667 13.24 2.323Q14.75 2.979 15.885"
          + " 4.115Q17.021 5.25 17.677 6.76Q18.333 8.271 18.333 10Q18.333 11.708 17.677"
          + " 13.229Q17.021 14.75 15.885 15.885Q14.75 17.021 13.24 17.677Q11.729 18.333 10"
          + " 18.333ZM13.312 6.646H15.667Q15.062 5.646 14.188 4.906Q13.312 4.167 12.25"
          + " 3.812Q12.604 4.438 12.875 5.167Q13.146 5.896 13.312 6.646ZM8.5 6.646H11.5Q11.271"
          + " 5.771 10.896 4.948Q10.521 4.125 10 3.396Q9.479 4.188 9.104 5Q8.729 5.812 8.5"
          + " 6.646ZM3.604 11.583H6.354Q6.312 11.167 6.281 10.771Q6.25 10.375 6.25 9.979Q6.25"
          + " 9.604 6.281 9.208Q6.312 8.812 6.354 8.396H3.604Q3.5 8.875 3.458 9.26Q3.417 9.646"
          + " 3.417 10Q3.417 10.354 3.458 10.729Q3.5 11.104 3.604 11.583ZM7.75 16.188Q7.417 15.583"
          + " 7.146 14.854Q6.875 14.125 6.708 13.333H4.354Q4.917 14.333 5.792 15.062Q6.667 15.792"
          + " 7.75 16.188ZM4.354 6.646H6.708Q6.896 5.854 7.156 5.135Q7.417 4.417 7.75 3.812Q6.688"
          + " 4.167 5.812 4.906Q4.938 5.646 4.354 6.646ZM10 16.583Q10.521 15.854 10.896"
          + " 15.031Q11.271 14.208 11.5 13.333H8.5Q8.729 14.167 9.104 14.979Q9.479 15.792 10"
          + " 16.583ZM8.104 11.583H11.896Q11.958 11.042 11.99 10.677Q12.021 10.312 12.021"
          + " 9.979Q12.021 9.667 11.99 9.302Q11.958 8.938 11.896 8.396H8.104Q8.062 8.812 8.031"
          + " 9.208Q8 9.604 8 9.979Q8 10.375 8.031 10.771Q8.062 11.167 8.104 11.583ZM12.25"
          + " 16.188Q13.333 15.792 14.219 15.062Q15.104 14.333 15.667 13.333H13.312Q13.146 14.083"
          + " 12.875 14.812Q12.604 15.542 12.25 16.188ZM13.646 11.583H16.396Q16.5 11.208 16.542"
          + " 10.812Q16.583 10.417 16.583 9.979Q16.583 9.562 16.542 9.167Q16.5 8.771 16.396"
          + " 8.396H13.646Q13.708 8.938 13.74 9.302Q13.771 9.667 13.771 9.979Q13.771 10.312 13.74"
          + " 10.677Q13.708 11.042 13.646 11.583Z";
  public static final String MORE_VERT_PATH =
      "M10.021 16.667Q9.354 16.667 8.875 16.188Q8.396 15.708 8.396 15.042Q8.396 14.375 8.875"
          + " 13.896Q9.354 13.417 10.021 13.417Q10.688 13.417 11.167 13.896Q11.646 14.375 11.646"
          + " 15.042Q11.646 15.708 11.167 16.188Q10.688 16.667 10.021 16.667ZM10.021 11.625Q9.354"
          + " 11.625 8.875 11.146Q8.396 10.667 8.396 10Q8.396 9.333 8.875 8.854Q9.354 8.375 10.021"
          + " 8.375Q10.688 8.375 11.167 8.854Q11.646 9.333 11.646 10Q11.646 10.667 11.167"
          + " 11.146Q10.688 11.625 10.021 11.625ZM10.021 6.583Q9.354 6.583 8.875 6.104Q8.396 5.625"
          + " 8.396 4.958Q8.396 4.292 8.875 3.813Q9.354 3.333 10.021 3.333Q10.688 3.333 11.167"
          + " 3.813Q11.646 4.292 11.646 4.958Q11.646 5.625 11.167 6.104Q10.688 6.583 10.021 6.583Z";
  // Person
  public static final String NAME_SVG_PATH =
      "M12 12Q10.35 12 9.175 10.825Q8 9.65 8 8Q8 6.35 9.175 5.175Q10.35 4 12 4Q13.65 4 14.825"
          + " 5.175Q16 6.35 16 8Q16 9.65 14.825 10.825Q13.65 12 12 12ZM4 20V17.2Q4 16.35 4.438"
          + " 15.637Q4.875 14.925 5.6 14.55Q7.15 13.775 8.75 13.387Q10.35 13 12 13Q13.65 13 15.25"
          + " 13.387Q16.85 13.775 18.4 14.55Q19.125 14.925 19.562 15.637Q20 16.35 20 17.2V20ZM6"
          + " 18H18V17.2Q18 16.925 17.863 16.7Q17.725 16.475 17.5 16.35Q16.15 15.675 14.775"
          + " 15.337Q13.4 15 12 15Q10.6 15 9.225 15.337Q7.85 15.675 6.5 16.35Q6.275 16.475 6.138"
          + " 16.7Q6 16.925 6 17.2ZM12 10Q12.825 10 13.413 9.412Q14 8.825 14 8Q14 7.175 13.413"
          + " 6.588Q12.825 6 12 6Q11.175 6 10.588 6.588Q10 7.175 10 8Q10 8.825 10.588 9.412Q11.175"
          + " 10 12 10ZM12 8Q12 8 12 8Q12 8 12 8Q12 8 12 8Q12 8 12 8Q12 8 12 8Q12 8 12 8Q12 8 12"
          + " 8Q12 8 12 8ZM12 18Q12 18 12 18Q12 18 12 18Q12 18 12 18Q12 18 12 18Q12 18 12 18Q12 18"
          + " 12 18Q12 18 12 18Q12 18 12 18Z";
  public static final String NOISE_CONTROL_OFF_SVG_PATH =
      "M10 14.208Q8.25 14.208 7.021 12.979Q5.792 11.75 5.792 10Q5.792 8.25 7.021 7.021Q8.25 5.792"
          + " 10 5.792Q11.75 5.792 12.979 7.021Q14.208 8.25 14.208 10Q14.208 11.75 12.979"
          + " 12.979Q11.75 14.208 10 14.208Z";
  // Numbers
  public static final String NUMBER_SVG_PATH =
      "M20.5,10L21,8h-4l1-4h-2l-1,4h-4l1-4h-2L9,8H5l-0.5,2h4l-1,4h-4L3,16h4l-1,4h2l1-4h4l-1,4h2l1-4h4l0.5-2h-4l1-4H20.5z"
          + " M13.5,14h-4l1-4h4L13.5,14z";

  // External Links
  public static final String OPEN_IN_NEW_PATH =
      "M19 19H5V5H12V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1"
          + " 21 21 20.1 21 19V12H19V19ZM14 3V5H17.59L7.76 14.83L9.17 16.24L19 6.41V10H21V3H14Z";

  public static final String PLUS_SVG_PATH =
      "M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z";
  public static final String PUBLISH_SVG_PATH =
      "M9.125 16.667V10.104L6.958 12.271L5.708 11.021L10 6.729L14.292 11.021L13.042 12.271L10.875"
          + " 10.104V16.667ZM3.333 7.5V5.083Q3.333 4.354 3.844 3.844Q4.354 3.333 5.083"
          + " 3.333H14.917Q15.646 3.333 16.156 3.844Q16.667 4.354 16.667"
          + " 5.083V7.5H14.917V5.083Q14.917 5.083 14.917 5.083Q14.917 5.083 14.917"
          + " 5.083H5.083Q5.083 5.083 5.083 5.083Q5.083 5.083 5.083 5.083V7.5Z";
  public static final String SEARCH_SVG_PATH =
      "M55.146,51.887L41.588,37.786c3.486-4.144,5.396-9.358,5.396-14.786c0-12.682-10.318-23-23-23s-23,10.318-23,23"
          + "  s10.318,23,23,23c4.761,0,9.298-1.436,13.177-4.162l13.661,14.208c0.571,0.593,1.339,0.92,2.162,0.92"
          + "  c0.779,0,1.518-0.297,2.079-0.837C56.255,54.982,56.293,53.08,55.146,51.887z"
          + " M23.984,6c9.374,0,17,7.626,17,17s-7.626,17-17,17 s-17-7.626-17-17S14.61,6,23.984,6z";
  // Radio Button Checked
  public static final String RADIO_BUTTON_PATH =
      "M12 17Q14.075 17 15.538 15.537Q17 14.075 17 12Q17 9.925 15.538 8.462Q14.075 7 12 7Q9.925 7"
          + " 8.463 8.462Q7 9.925 7 12Q7 14.075 8.463 15.537Q9.925 17 12 17ZM12 22Q9.925 22 8.1"
          + " 21.212Q6.275 20.425 4.925 19.075Q3.575 17.725 2.788 15.9Q2 14.075 2 12Q2 9.925 2.788"
          + " 8.1Q3.575 6.275 4.925 4.925Q6.275 3.575 8.1 2.787Q9.925 2 12 2Q14.075 2 15.9"
          + " 2.787Q17.725 3.575 19.075 4.925Q20.425 6.275 21.212 8.1Q22 9.925 22 12Q22 14.075"
          + " 21.212 15.9Q20.425 17.725 19.075 19.075Q17.725 20.425 15.9 21.212Q14.075 22 12"
          + " 22ZM12 12Q12 12 12 12Q12 12 12 12Q12 12 12 12Q12 12 12 12Q12 12 12 12Q12 12 12 12Q12"
          + " 12 12 12Q12 12 12 12ZM12 20Q15.325 20 17.663 17.663Q20 15.325 20 12Q20 8.675 17.663"
          + " 6.337Q15.325 4 12 4Q8.675 4 6.338 6.337Q4 8.675 4 12Q4 15.325 6.338 17.663Q8.675 20"
          + " 12 20Z";
  // Notes
  public static final String TEXT_SVG_PATH = "M21 11.01L3 11v2h18zM3 16h12v2H3zM21 6H3v2.01L21 8z";
  public static final String TEXT_SNIPPET_SVG_PATH =
      "M4.25 15.75H15.75Q15.75 15.75 15.75 15.75Q15.75 15.75 15.75 15.75V8.188L11.812"
          + " 4.25H4.25Q4.25 4.25 4.25 4.25Q4.25 4.25 4.25 4.25V15.75Q4.25 15.75 4.25 15.75Q4.25"
          + " 15.75 4.25 15.75ZM4.25 17.5Q3.521 17.5 3.01 16.99Q2.5 16.479 2.5 15.75V4.25Q2.5"
          + " 3.521 3.01 3.01Q3.521 2.5 4.25 2.5H12.542L17.5 7.458V15.75Q17.5 16.479 16.99"
          + " 16.99Q16.479 17.5 15.75 17.5ZM5.833 14.208H14.167V12.458H5.833ZM5.833"
          + " 10.875H14.167V9.125H5.833ZM5.833 7.521H11.625V5.771H5.833ZM4.25 15.75Q4.25 15.75"
          + " 4.25 15.75Q4.25 15.75 4.25 15.75V4.25Q4.25 4.25 4.25 4.25Q4.25 4.25 4.25"
          + " 4.25V8.188V15.75Q4.25 15.75 4.25 15.75Q4.25 15.75 4.25 15.75Z";
  public static final String UNKNOWN_SVG_PATH =
      "M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0"
          + " 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4"
          + " 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5"
          + " 0-2.21-1.79-4-4-4z";
  public static final String TRASH_CAN_SVG_PATH =
      "M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1"
          + " 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16";
  public static final String VISIBILITY_SVG_PATH =
      "M10 13.354Q11.583 13.354 12.688 12.25Q13.792 11.146 13.792 9.562Q13.792 7.979 12.688"
          + " 6.875Q11.583 5.771 10 5.771Q8.417 5.771 7.312 6.875Q6.208 7.979 6.208 9.562Q6.208"
          + " 11.146 7.312 12.25Q8.417 13.354 10 13.354ZM10 11.812Q9.062 11.812 8.406 11.156Q7.75"
          + " 10.5 7.75 9.562Q7.75 8.625 8.406 7.969Q9.062 7.312 10 7.312Q10.938 7.312 11.594"
          + " 7.969Q12.25 8.625 12.25 9.562Q12.25 10.5 11.594 11.156Q10.938 11.812 10 11.812ZM10"
          + " 15.833Q6.938 15.833 4.448 14.125Q1.958 12.417 0.833 9.562Q1.958 6.708 4.458"
          + " 5.021Q6.958 3.333 10 3.333Q13.042 3.333 15.542 5.021Q18.042 6.708 19.167"
          + " 9.562Q18.042 12.417 15.552 14.125Q13.062 15.833 10 15.833ZM10 9.562Q10 9.562 10"
          + " 9.562Q10 9.562 10 9.562Q10 9.562 10 9.562Q10 9.562 10 9.562Q10 9.562 10 9.562Q10"
          + " 9.562 10 9.562Q10 9.562 10 9.562Q10 9.562 10 9.562ZM10 14.083Q12.333 14.083 14.281"
          + " 12.865Q16.229 11.646 17.25 9.562Q16.229 7.479 14.281 6.281Q12.333 5.083 10"
          + " 5.083Q7.667 5.083 5.708 6.281Q3.75 7.479 2.729 9.562Q3.75 11.646 5.708 12.865Q7.667"
          + " 14.083 10 14.083Z";
  public static final String WARNING_SVG_PATH =
      "M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742"
          + " 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012"
          + " 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z";

  public static final String ACCORDION_BUTTON_PATH = "M19 9l-7 7-7-7";

  public static SvgTag questionTypeSvg(QuestionType type, int size) {
    return questionTypeSvg(type, size, size);
  }

  public static SvgTag questionTypeSvg(QuestionType type, int width, int height) {
    String iconPath = "";
    switch (type) {
      case ADDRESS:
        iconPath = Icons.ADDRESS_SVG_PATH;
        break;
      case CHECKBOX:
        iconPath = Icons.CHECKBOX_SVG_PATH;
        break;
      case CURRENCY:
        return svg(Icons.CURRENCY_SVG_PATH, width, height)
            .attr("fill", "none")
            .attr("stroke-linecap", "round")
            .attr("stroke-linejoin", "round")
            .attr("stroke-width", "2");
      case DATE:
        iconPath = Icons.DATE_SVG_PATH;
        break;
      case DROPDOWN:
        iconPath = Icons.DROPDOWN_SVG_PATH;
        break;
      case EMAIL:
        iconPath = Icons.EMAIL_SVG_PATH;
        break;
      case FILEUPLOAD:
        iconPath = Icons.FILEUPLOAD_SVG_PATH;
        break;
      case ID:
        iconPath = Icons.ID_SVG_PATH;
        break;
      case NAME:
        iconPath = Icons.NAME_SVG_PATH;
        break;
      case NUMBER:
        iconPath = Icons.NUMBER_SVG_PATH;
        break;
      case RADIO_BUTTON:
        iconPath = Icons.RADIO_BUTTON_PATH;
        break;
      case ENUMERATOR:
        iconPath = Icons.ENUMERATOR_SVG_PATH;
        break;
      case STATIC:
        return svg(Icons.ANNOTATION_SVG_PATH, width, height)
            .attr("fill", "none")
            .attr("stroke-linecap", "round")
            .attr("stroke-linejoin", "round")
            .attr("stroke-width", "2");
      case TEXT:
        iconPath = Icons.TEXT_SVG_PATH;
        break;
      default:
        iconPath = Icons.UNKNOWN_SVG_PATH;
    }
    return svg(iconPath, width, height);
  }

  public static SvgTag svg(String pathString, int pixelSize) {
    return svg(pathString, pixelSize, pixelSize);
  }

  public static SvgTag svg(String pathString, int width, int height) {
    // Setting the viewBox to a specific height/width is insufficient to
    // actually cause the SVG's bounds to match. Here, the width / height
    // of the SVG element are explicitly set, which is more consistent
    // with what one would expect given the method signature.
    return svg(pathString)
        .attr("viewBox", String.format("0 0 %1$d %2$d", width, height))
        .withWidth(String.valueOf(width))
        .withHeight(String.valueOf(height));
  }

  private static SvgTag svg(String pathString) {
    return svg().with(path(pathString));
  }

  private static SvgTag svg() {
    return new SvgTag()
        .attr("xmlns", "http://www.w3.org/2000/svg")
        .attr("fill", "currentColor")
        .attr("stroke", "currentColor")
        .attr("stroke-width", "1%")
        .attr("aria-hidden", "true");
  }

  private static PathTag path(String pathString) {
    return new PathTag().attr("d", pathString);
  }
}
