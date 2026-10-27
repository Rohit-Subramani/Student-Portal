package com.college.studentportal.repository;

import com.college.studentportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Email comparison is case insensitive so that A@x.com and a@x.com cannot both register. */
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByResetToken(String resetToken);
}
