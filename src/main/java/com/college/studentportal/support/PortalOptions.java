package com.college.studentportal.support;

import java.util.List;

/** Fixed option lists shared by the controllers and the templates. */
public final class PortalOptions {

    public static final List<String> DEPARTMENTS = List.of(
            "Computer Science",
            "Information Technology",
            "Electronics",
            "Electrical",
            "Mechanical",
            "Civil");

    public static final List<String> INTERESTS = List.of(
            "Coding",
            "Robotics",
            "Sports",
            "Music",
            "Debate");

    public static final List<String> GENDERS = List.of("Male", "Female", "Other");

    private PortalOptions() {
    }
}
