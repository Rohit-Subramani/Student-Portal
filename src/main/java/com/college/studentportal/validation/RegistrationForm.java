package com.college.studentportal.validation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Backing object of the registration page. */
public class RegistrationForm {

    /** At least 8 characters with an uppercase letter, a lowercase letter, a digit and a special character. */
    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";

    public static final String PASSWORD_MESSAGE =
            "Password must be at least 8 characters long and include an uppercase letter, a lowercase letter, a digit and a special character.";

    /** Shown for the class level rule below (and reused by the controller for the inline message). */
    public static final String PASSWORD_MISMATCH_MESSAGE = "Passwords do not match.";

    /**
     * A phone number must be exactly ten digits.
     * Anything else, including spaces, letters, hyphens, punctuation and a country code
     * prefix, does not match. Required-field presence is separately enforced by {@code @NotBlank}.
     */
    public static final String PHONE_REGEX = "^\\d{10}$";

    public static final String PHONE_MESSAGE =
            "Phone number must be exactly 10 digits and contain digits only (no spaces, letters, punctuation or country code).";

    /** Upper bound for the address field, mirrored by the {@code maxlength} of the input. */
    public static final int ADDRESS_MAX_LENGTH = 200;

    public static final String ADDRESS_TOO_LONG_MESSAGE =
            "Address must not be longer than " + ADDRESS_MAX_LENGTH + " characters.";

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 60, message = "Full name must be between 2 and 60 characters.")
    private String name = "";

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address (for example student@college.edu).")
    @Size(max = 120, message = "Email must not be longer than 120 characters.")
    private String email = "";

    @NotBlank(message = "Password is required.")
    @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
    private String password = "";

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword = "";

    @NotBlank(message = "Please select a department.")
    private String department = "";

    @NotBlank(message = "Please select a gender.")
    private String gender = "";

    @Size(min = 1, message = "Please select at least one interest.")
    private List<String> interests = new ArrayList<>();

    @NotNull(message = "Date of birth is required.")
    @Past(message = "Date of birth must be a date in the past.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = PHONE_REGEX, message = PHONE_MESSAGE)
    private String phoneNumber = "";

    @NotBlank(message = "Address is required.")
    @Size(max = ADDRESS_MAX_LENGTH, message = ADDRESS_TOO_LONG_MESSAGE)
    private String address = "";

    @AssertTrue(message = PASSWORD_MISMATCH_MESSAGE)
    public boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) {
            // Blank passwords are reported by the individual @NotBlank constraints.
            return true;
        }
        return password.equals(confirmPassword);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests == null ? new ArrayList<>() : interests;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
