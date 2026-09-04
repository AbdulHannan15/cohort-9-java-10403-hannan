package com.Contact.Management.System.Cms.Service;

import com.Contact.Management.System.Cms.DTO.ChangePasswordRequest;
import com.Contact.Management.System.Cms.DTO.LoginRequest;
import com.Contact.Management.System.Cms.DTO.RegisterRequest;
import com.Contact.Management.System.Cms.DTO.UserResponse;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.Exception.DuplicateResourceException;
import com.Contact.Management.System.Cms.Exception.InvalidCredentialsException;
import com.Contact.Management.System.Cms.Exception.ResourceNotFoundException;
import com.Contact.Management.System.Cms.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Handles user self-registration (via email or phone), login authentication,
 * and password changes. Passwords are always hashed via {@link PasswordEncoder}
 * and the raw hash is never returned to callers.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user");

        if (!StringUtils.hasText(request.getLoginIdentifier())) {
            throw new IllegalArgumentException("Login identifier (email or phone) is required");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepo.existsByLoginIdentifier(request.getLoginIdentifier())) {
            log.warn("Registration failed - loginIdentifier already in use: {}", request.getLoginIdentifier());
            throw new DuplicateResourceException("An account with this email/phone already exists");
        }
        if (StringUtils.hasText(request.getRecoveryPhone())
                && userRepo.existsByRecoveryPhone(request.getRecoveryPhone())) {
            log.warn("Registration failed - recoveryPhone already in use: {}", request.getRecoveryPhone());
            throw new DuplicateResourceException("This recovery phone is already registered");
        }

        User user = User.builder()
                .loginIdentifier(request.getLoginIdentifier())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .recoveryPhone(request.getRecoveryPhone())
                .build();

        User saved;
        try {
            saved = userRepo.save(user);
        } catch (DataIntegrityViolationException e) {
            // Guards against a race where two concurrent registrations both pass the
            // existsBy* checks above and then hit the unique constraint on save.
            log.warn("Registration failed - duplicate loginIdentifier or recoveryPhone on save");
            throw new DuplicateResourceException("An account with this email/phone already exists");
        }
        log.info("User registered successfully with id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        log.info("Login attempt for loginIdentifier={}", request.getLoginIdentifier());

        User user = userRepo.findByLoginIdentifier(request.getLoginIdentifier())
                .orElseThrow(() -> {
                    log.warn("Login failed - no account for loginIdentifier={}", request.getLoginIdentifier());
                    return new InvalidCredentialsException("Invalid login credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed - password mismatch for userId={}", user.getId());
            throw new InvalidCredentialsException("Invalid login credentials");
        }

        log.info("Login successful for userId={}", user.getId());
        return toResponse(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Change password requested for userId={}", userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            log.warn("Change password failed - old password mismatch for userId={}", userId);
            throw new InvalidCredentialsException("Old password is incorrect");
        }
        if (!StringUtils.hasText(request.getNewPassword())) {
            throw new IllegalArgumentException("New password is required");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
        log.info("Password changed successfully for userId={}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .loginIdentifier(user.getLoginIdentifier())
                .recoveryPhone(user.getRecoveryPhone())
                .role(user.getRole())
                .build();
    }
}
