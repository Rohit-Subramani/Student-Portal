package com.college.studentportal.controller;

import com.college.studentportal.service.UserService;
import com.college.studentportal.support.FormErrors;
import com.college.studentportal.validation.PasswordResetConfirmForm;
import com.college.studentportal.validation.PasswordResetRequestForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Local demo password reset workflow.
 *
 * <p>Step 1 generates a one time token and shows it on the page, step 2 consumes the token and
 * stores a new hash. Nothing is emailed: this project must not pretend to send emails.</p>
 */
@Controller
public class PasswordResetController {

    private final UserService userService;

    public PasswordResetController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/reset-password")
    public String showResetPage(Model model) {
        addPageAttributes(model, FormErrors.fieldMessages(null), FormErrors.allMessages(null), null);
        return "reset-password";
    }

    @PostMapping("/reset-password/request")
    public String requestResetToken(@Valid @ModelAttribute("requestForm") PasswordResetRequestForm form,
                                    BindingResult bindingResult,
                                    Model model) {
        String token = null;
        if (!bindingResult.hasErrors()) {
            Optional<String> generated = userService.createResetToken(form.getEmail());
            if (generated.isPresent()) {
                token = generated.get();
                model.addAttribute("successMessage",
                        "Demo mode: a one time reset token was generated locally. No email is sent by this application.");
            } else {
                bindingResult.reject("unknownEmail", "No account found for that email address.");
            }
        }
        addPageAttributes(model, FormErrors.fieldMessages(bindingResult),
                FormErrors.allMessages(bindingResult), token);
        return "reset-password";
    }

    @PostMapping("/reset-password/confirm")
    public String confirmReset(@Valid @ModelAttribute("confirmForm") PasswordResetConfirmForm form,
                               BindingResult bindingResult,
                               Model model) {
        if (!bindingResult.hasErrors()) {
            if (userService.resetPassword(form.getToken(), form.getNewPassword())) {
                addPageAttributes(model, FormErrors.fieldMessages(null), FormErrors.allMessages(null), null);
                model.addAttribute("successMessage",
                        "Password reset successful. Please sign in with your new password.");
                return "reset-password";
            }
            bindingResult.reject("invalidToken",
                    "That reset token is not valid or has expired. Please request a new one.");
        }
        Map<String, String> fieldErrors = FormErrors.fieldMessages(bindingResult);
        // "New passwords do not match." is a class level rule: also show it under the confirmation
        // input, while the page level banner keeps listing every message of the failed submit.
        if (!form.isNewPasswordConfirmed()) {
            fieldErrors.put("confirmNewPassword", PasswordResetConfirmForm.NEW_PASSWORD_MISMATCH_MESSAGE);
        }
        addPageAttributes(model, fieldErrors, FormErrors.allMessages(bindingResult), form.getToken());
        model.addAttribute("confirmForm", form);
        return "reset-password";
    }

    private void addPageAttributes(Model model, Map<String, String> fieldErrors,
                                   List<String> errorMessages, String demoToken) {
        if (!model.containsAttribute("requestForm")) {
            model.addAttribute("requestForm", new PasswordResetRequestForm());
        }
        PasswordResetConfirmForm confirmForm = new PasswordResetConfirmForm();
        if (demoToken != null) {
            confirmForm.setToken(demoToken);
        }
        model.addAttribute("confirmForm", confirmForm);
        model.addAttribute("demoToken", demoToken == null ? "" : demoToken);
        model.addAttribute("resetTokenRequested", demoToken != null);
        model.addAttribute("fieldErrors", fieldErrors);
        model.addAttribute("errorMessages", errorMessages);
    }
}
