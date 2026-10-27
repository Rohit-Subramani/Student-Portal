package com.college.studentportal.support;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link BindingResult} into plain model attributes so that the Thymeleaf
 * templates can render stable elements ({@code #errorMessage} and {@code #<field>Error})
 * without depending on the Spring {@code #fields} helper.
 */
public final class FormErrors {

    private FormErrors() {
    }

    /** @return the first error message per field, keyed by field name. */
    public static Map<String, String> fieldMessages(BindingResult result) {
        Map<String, String> messages = new LinkedHashMap<>();
        if (result == null) {
            return messages;
        }
        for (FieldError error : result.getFieldErrors()) {
            messages.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return messages;
    }

    /** @return every validation message (field errors and object errors) in binding order. */
    public static List<String> allMessages(BindingResult result) {
        List<String> messages = new ArrayList<>();
        if (result == null) {
            return messages;
        }
        for (ObjectError error : result.getAllErrors()) {
            messages.add(error.getDefaultMessage());
        }
        return messages;
    }
}
