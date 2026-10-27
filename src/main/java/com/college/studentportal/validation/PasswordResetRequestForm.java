package com.college.studentportal.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Backing object of the "request a reset token" step. */
public class PasswordResetRequestForm {

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address (for example student@college.edu).")
    private String email = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
