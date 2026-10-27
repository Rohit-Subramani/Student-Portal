package com.college.studentportal.tests.support;

import java.util.ArrayList;
import java.util.List;

/** One row of {@code student_portal_testdata.csv}. */
public class TestRecord {

    private final String testCaseId;
    private final Scenario scenario;
    private final String description;
    private final String name;
    private final String email;
    private final String password;
    private final String confirmPassword;
    private final String department;
    private final String gender;
    private final String interests;
    private final String dateOfBirth;
    private final boolean expectSuccess;
    private final String expectedMessage;

    public TestRecord(String testCaseId, Scenario scenario, String description, String name, String email,
                      String password, String confirmPassword, String department, String gender, String interests,
                      String dateOfBirth, boolean expectSuccess, String expectedMessage) {
        this.testCaseId = testCaseId;
        this.scenario = scenario;
        this.description = description;
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.department = department;
        this.gender = gender;
        this.interests = interests;
        this.dateOfBirth = dateOfBirth;
        this.expectSuccess = expectSuccess;
        this.expectedMessage = expectedMessage;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getDepartment() {
        return department;
    }

    public String getGender() {
        return gender;
    }

    public String getInterests() {
        return interests;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean isExpectSuccess() {
        return expectSuccess;
    }

    public String getExpectedMessage() {
        return expectedMessage;
    }

    /** Split interests on {@code |}, for example {@code Coding|Robotics}. */
    public List<String> getInterestList() {
        List<String> values = new ArrayList<>();
        if (interests == null || interests.isBlank()) {
            return values;
        }
        for (String value : interests.split("\\|")) {
            if (!value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }

    @Override
    public String toString() {
        return testCaseId + " " + scenario + " - " + description;
    }
}
