package com.college.studentportal.controller;

import com.college.studentportal.model.User;
import com.college.studentportal.service.UserService;
import com.college.studentportal.support.SessionAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/** Makes the signed in student available to every template (used by the shared navigation bar). */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;

    public GlobalModelAttributes(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addSessionAttributes(HttpSession session, Model model) {
        boolean signedIn = false;
        Object userId = session.getAttribute(SessionAttributes.USER_ID);
        if (userId instanceof Long id) {
            Optional<User> user = userService.findById(id);
            if (user.isPresent()) {
                signedIn = true;
                model.addAttribute("signedInUser", user.get().getFullName());
            }
        }
        model.addAttribute("signedIn", signedIn);
    }
}
