package com.college.studentportal.service;

import com.college.studentportal.model.User;
import com.college.studentportal.repository.UserRepository;
import com.college.studentportal.validation.ProfileForm;
import com.college.studentportal.validation.RegistrationForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/** Registration, login, profile updates and the local demo password reset workflow. */
@Service
public class UserService {

    /** How long a locally generated demo reset token stays usable. */
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);
    private static final String TOKEN_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public User register(RegistrationForm form) {
        String email = normalise(form.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(
                    "This email address is already registered. Please sign in or use a different email.");
        }
        User user = new User(
                form.getName().trim(),
                email,
                passwordHasher.hash(form.getPassword()),
                form.getDepartment(),
                form.getGender(),
                form.getInterests(),
                form.getDateOfBirth(),
                form.getPhoneNumber().trim(),
                form.getAddress().trim());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(normalise(email))
                .filter(user -> passwordHasher.matches(rawPassword, user.getPasswordHash()));
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Transactional
    public void updateProfile(Long userId, ProfileForm form) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Signed in user no longer exists: " + userId));
        user.setFullName(form.getName().trim());
        user.setDepartment(form.getDepartment());
        user.setInterestList(form.getInterests());
        user.setPhoneNumber(form.getPhoneNumber().trim());
        user.setAddress(form.getAddress().trim());
        userRepository.save(user);
    }

    /**
     * Creates a reset token for the given email address.
     *
     * <p>The token is only returned to the caller and displayed on the reset page. No email is
     * sent anywhere: this project is a local demonstration of the reset workflow.</p>
     *
     * @return the token, or empty when no account uses that email address
     */
    @Transactional
    public Optional<String> createResetToken(String email) {
        Optional<User> found = userRepository.findByEmailIgnoreCase(normalise(email));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        String token = generateToken();
        User user = found.get();
        user.setResetToken(token);
        user.setResetTokenCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return Optional.of(token);
    }

    /**
     * Applies a reset token coming from the local demo page.
     *
     * @return {@code true} when the token was valid and the password was changed
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Optional<User> found = userRepository.findByResetToken(token.trim().toUpperCase(Locale.ROOT));
        if (found.isEmpty()) {
            return false;
        }
        User user = found.get();
        LocalDateTime createdAt = user.getResetTokenCreatedAt();
        if (createdAt == null || createdAt.plus(RESET_TOKEN_TTL).isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setPasswordHash(passwordHasher.hash(newPassword));
        user.setResetToken(null);
        user.setResetTokenCreatedAt(null);
        userRepository.save(user);
        return true;
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            token.append(TOKEN_ALPHABET.charAt(RANDOM.nextInt(TOKEN_ALPHABET.length())));
        }
        return token.toString();
    }

    private String normalise(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
