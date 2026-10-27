package com.college.studentportal.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Backing object of the login page. */
public class LoginForm {

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address (for example student@college.edu).")
    private String email = "";

    @NotBlank(message = "Password is required.")
    private String password = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
