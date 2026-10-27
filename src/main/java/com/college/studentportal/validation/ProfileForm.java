package com.college.studentportal.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/** Backing object of the editable profile page (name, department, preferences, phone and address). */
public class ProfileForm {

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 60, message = "Full name must be between 2 and 60 characters.")
    private String name = "";

    @NotBlank(message = "Please select a department.")
    private String department = "";

    @Size(min = 1, message = "Please select at least one interest.")
    private List<String> interests = new ArrayList<>();

    /** Same rule as the registration page: required, exactly ten digits, digits only. */
    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = RegistrationForm.PHONE_REGEX, message = RegistrationForm.PHONE_MESSAGE)
    private String phoneNumber = "";

    @NotBlank(message = "Address is required.")
    @Size(max = RegistrationForm.ADDRESS_MAX_LENGTH, message = RegistrationForm.ADDRESS_TOO_LONG_MESSAGE)
    private String address = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests == null ? new ArrayList<>() : interests;
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
