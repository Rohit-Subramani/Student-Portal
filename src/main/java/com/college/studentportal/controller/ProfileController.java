package com.college.studentportal.controller;

import com.college.studentportal.model.User;
import com.college.studentportal.service.UserService;
import com.college.studentportal.support.FormErrors;
import com.college.studentportal.support.PortalOptions;
import com.college.studentportal.support.SessionAttributes;
import com.college.studentportal.validation.ProfileForm;
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

/** Profile page: the student can change the name, the department and the preferences. */
@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }
        User user = currentUser.get();
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setName(user.getFullName());
            form.setDepartment(user.getDepartment());
            form.setInterests(user.getInterestList());
            form.setPhoneNumber(user.getPhoneNumber());
            form.setAddress(user.getAddress());
            model.addAttribute("profileForm", form);
        }
        addProfileViewAttributes(model, user, Map.of(), List.of());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = currentUser(session);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }
        User user = currentUser.get();
        if (!bindingResult.hasErrors()) {
            userService.updateProfile(user.getId(), form);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
            return "redirect:/profile";
        }
        addProfileViewAttributes(model, user, FormErrors.fieldMessages(bindingResult),
                FormErrors.allMessages(bindingResult));
        return "profile";
    }

    private void addProfileViewAttributes(Model model, User user,
                                          Map<String, String> fieldErrors, List<String> errorMessages) {
        model.addAttribute("profileEmail", user.getEmail());
        model.addAttribute("profileGender", user.getGender());
        model.addAttribute("profilePhone", blankIfNull(user.getPhoneNumber()));
        model.addAttribute("profileAddress", blankIfNull(user.getAddress()));
        model.addAttribute("profileDateOfBirth",
                user.getDateOfBirth() == null ? "" : user.getDateOfBirth().toString());
        model.addAttribute("departments", PortalOptions.DEPARTMENTS);
        model.addAttribute("interestOptions", PortalOptions.INTERESTS);
        model.addAttribute("fieldErrors", fieldErrors);
        model.addAttribute("errorMessages", errorMessages);
    }

    /** Rows created before the phone number and address existed have no value for them. */
    private static String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private Optional<User> currentUser(HttpSession session) {
        Object userId = session.getAttribute(SessionAttributes.USER_ID);
        if (userId instanceof Long id) {
            return userService.findById(id);
        }
        return Optional.empty();
    }
}
