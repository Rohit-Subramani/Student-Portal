package com.college.studentportal.controller;

import com.college.studentportal.service.UserService;
import com.college.studentportal.support.FormErrors;
import com.college.studentportal.support.PortalOptions;
import com.college.studentportal.support.SessionAttributes;
import com.college.studentportal.model.User;
import com.college.studentportal.service.DuplicateEmailException;
import com.college.studentportal.validation.LoginForm;
import com.college.studentportal.validation.RegistrationForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Registration, sign in and sign out. */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        addRegistrationOptions(model);
        model.addAttribute("fieldErrors", Map.of());
        model.addAttribute("errorMessages", List.of());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                userService.register(form);
            } catch (DuplicateEmailException ex) {
                bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            }
        }
        if (bindingResult.hasErrors()) {
            addRegistrationOptions(model);
            Map<String, String> fieldErrors = FormErrors.fieldMessages(bindingResult);
            // "Passwords do not match." comes from a class level rule, so Bean Validation does not
            // attach it to a field. Surface it underneath the confirmation input as well; the page
            // level banner still lists every message of the failed submit.
            if (!form.isPasswordConfirmed()) {
                fieldErrors.put("confirmPassword", RegistrationForm.PASSWORD_MISMATCH_MESSAGE);
            }
            model.addAttribute("fieldErrors", fieldErrors);
            model.addAttribute("errorMessages", FormErrors.allMessages(bindingResult));
            return "register";
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Registration successful. Please sign in with your new account.");
        redirectAttributes.addFlashAttribute("registeredEmail", form.getEmail());
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        model.addAttribute("fieldErrors", Map.of());
        model.addAttribute("errorMessages", List.of());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        BindingResult bindingResult,
                        Model model,
                        HttpSession session) {
        if (!bindingResult.hasErrors()) {
            Optional<User> user = userService.authenticate(form.getEmail(), form.getPassword());
            if (user.isPresent()) {
                session.setAttribute(SessionAttributes.USER_ID, user.get().getId());
                return "redirect:/profile";
            }
            bindingResult.reject("invalidCredentials", "Invalid email or password.");
        }
        model.addAttribute("fieldErrors", FormErrors.fieldMessages(bindingResult));
        model.addAttribute("errorMessages", FormErrors.allMessages(bindingResult));
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been signed out successfully.");
        return "redirect:/login";
    }

    private void addRegistrationOptions(Model model) {
        model.addAttribute("departments", PortalOptions.DEPARTMENTS);
        model.addAttribute("genders", PortalOptions.GENDERS);
        model.addAttribute("interestOptions", PortalOptions.INTERESTS);
    }
}
