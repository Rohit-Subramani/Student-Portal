package com.college.studentportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Registered student account.
 *
 * <p>Passwords are never stored in plain text: only the PBKDF2 hash produced by
 * {@link com.college.studentportal.service.PasswordHasher} is persisted.</p>
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 60)
    private String fullName;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    /** PBKDF2 hash, never the raw password. */
    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "department", nullable = false, length = 60)
    private String department;

    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    /** Preferences stored as a comma separated list, e.g. {@code Coding,Music}. */
    @Column(name = "interests", length = 300)
    private String interests;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * Contact number of the student. Stored as text (not as a number) so that leading zeros
     * survive. The column is deliberately left nullable so that rows created before this field
     * existed stay valid; "required, exactly 10 digits" is enforced on the form instead.
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /** Postal address of the student, stored without leading or trailing whitespace. */
    @Column(name = "address", length = 200)
    private String address;

    /** Local demo password reset token (no email is ever sent by this project). */
    @Column(name = "reset_token", length = 80)
    private String resetToken;

    @Column(name = "reset_token_created_at")
    private LocalDateTime resetTokenCreatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected User() {
        // Required by JPA.
    }

    public User(String fullName, String email, String passwordHash, String department,
                String gender, List<String> interests, LocalDate dateOfBirth,
                String phoneNumber, String address) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.department = department;
        this.gender = gender;
        setInterestList(interests);
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
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

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenCreatedAt() {
        return resetTokenCreatedAt;
    }

    public void setResetTokenCreatedAt(LocalDateTime resetTokenCreatedAt) {
        this.resetTokenCreatedAt = resetTokenCreatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** @return the stored preferences as a list (never {@code null}). */
    public List<String> getInterestList() {
        if (interests == null || interests.isBlank()) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        for (String value : interests.split(",")) {
            if (!value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }

    public void setInterestList(List<String> values) {
        if (values == null || values.isEmpty()) {
            this.interests = "";
            return;
        }
        this.interests = String.join(",", values);
    }

    /** Utility used by tests and debugging output. */
    public List<String> interestsAsArray() {
        return Arrays.asList(getInterestList().toArray(new String[0]));
    }
}
