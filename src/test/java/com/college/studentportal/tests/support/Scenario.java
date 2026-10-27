package com.college.studentportal.tests.support;

/** The kinds of scenarios that the CSV driven suite can execute. */
public enum Scenario {

    REGISTER_VALID,
    REGISTER_DUPLICATE_EMAIL,
    REGISTER_EMPTY_NAME,
    REGISTER_EMPTY_EMAIL,
    REGISTER_INVALID_EMAIL,
    REGISTER_WEAK_PASSWORD,
    REGISTER_MISMATCH_PASSWORD,
    REGISTER_EMPTY_DEPARTMENT,
    REGISTER_EMPTY_GENDER,
    REGISTER_NO_INTERESTS,
    REGISTER_EMPTY_DOB,
    REGISTER_EMPTY_PASSWORD,
    LOGIN_VALID,
    LOGIN_WRONG_PASSWORD,
    LOGIN_UNKNOWN_EMAIL,
    PROFILE_EDIT,
    PASSWORD_RESET_REQUEST_KNOWN,
    PASSWORD_RESET_REQUEST_UNKNOWN,
    PASSWORD_RESET_MISMATCH,
    PASSWORD_RESET_SUCCESS;

    /** Every server side validation scenario of the registration page. */
    public static Scenario[] registrationValidationScenarios() {
        return new Scenario[]{
                REGISTER_EMPTY_NAME,
                REGISTER_EMPTY_EMAIL,
                REGISTER_INVALID_EMAIL,
                REGISTER_WEAK_PASSWORD,
                REGISTER_MISMATCH_PASSWORD,
                REGISTER_EMPTY_DEPARTMENT,
                REGISTER_EMPTY_GENDER,
                REGISTER_NO_INTERESTS,
                REGISTER_EMPTY_DOB,
                REGISTER_EMPTY_PASSWORD};
    }

    public static Scenario[] loginScenarios() {
        return new Scenario[]{LOGIN_VALID, LOGIN_WRONG_PASSWORD, LOGIN_UNKNOWN_EMAIL};
    }

    public static Scenario[] passwordResetScenarios() {
        return new Scenario[]{
                PASSWORD_RESET_REQUEST_KNOWN,
                PASSWORD_RESET_REQUEST_UNKNOWN,
                PASSWORD_RESET_MISMATCH,
                PASSWORD_RESET_SUCCESS};
    }
}
