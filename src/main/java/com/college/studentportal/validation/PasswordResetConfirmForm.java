package com.college.studentportal.validation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Backing object of the local demo "confirm the reset" step. */
public class PasswordResetConfirmForm {

    /** Shown for the class level rule below (and reused by the controller for the inline message). */
    public static final String NEW_PASSWORD_MISMATCH_MESSAGE = "New passwords do not match.";

    @NotBlank(message = "Reset token is required.")
    private String token = "";

    @NotBlank(message = "New password is required.")
    @Pattern(regexp = RegistrationForm.PASSWORD_REGEX, message = RegistrationForm.PASSWORD_MESSAGE)
    private String newPassword = "";

    @NotBlank(message = "Please confirm the new password.")
    private String confirmNewPassword = "";

    @AssertTrue(message = NEW_PASSWORD_MISMATCH_MESSAGE)
    public boolean isNewPasswordConfirmed() {
        if (newPassword == null || confirmNewPassword == null) {
            return true;
        }
        return newPassword.equals(confirmNewPassword);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
