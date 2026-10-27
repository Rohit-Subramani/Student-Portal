package com.college.studentportal.service;

/** Thrown when a registration is attempted with an email address that already exists. */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
