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
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .loginIdentifier("jane@example.com")
                .passwordHash("hashed-pw")
                .recoveryPhone("+15551234567")
                .role(RoleEnum.USER)
                .build();
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates a user and hashes the password when identifier is unused")
        void register_success() {
            RegisterRequest request = new RegisterRequest("new@example.com", "plaintext-pw", null);
            when(userRepo.existsByLoginIdentifier("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("plaintext-pw")).thenReturn("hashed");
            when(userRepo.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(42L);
                return u;
            });

            UserResponse response = userService.register(request);

            assertThat(response.getId()).isEqualTo(42L);
            assertThat(response.getLoginIdentifier()).isEqualTo("new@example.com");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(captor.capture());
            assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
        }

        @Test
        @DisplayName("rejects a blank login identifier")
        void register_blankIdentifier_throws() {
            RegisterRequest request = new RegisterRequest("   ", "pw", null);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(userRepo);
        }

        @Test
        @DisplayName("rejects a blank password")
        void register_blankPassword_throws() {
            RegisterRequest request = new RegisterRequest("new@example.com", "", null);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a login identifier already in use")
        void register_duplicateIdentifier_throws() {
            RegisterRequest request = new RegisterRequest("jane@example.com", "pw", null);
            when(userRepo.existsByLoginIdentifier("jane@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicateResourceException.class);
            verify(userRepo, never()).save(any());
        }

        @Test
        @DisplayName("rejects a recovery phone already in use")
        void register_duplicateRecoveryPhone_throws() {
            RegisterRequest request = new RegisterRequest("new@example.com", "pw", "+15551234567");
            when(userRepo.existsByLoginIdentifier("new@example.com")).thenReturn(false);
            when(userRepo.existsByRecoveryPhone("+15551234567")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("returns the user when credentials match")
        void login_success() {
            LoginRequest request = new LoginRequest("jane@example.com", "plaintext-pw");
            when(userRepo.findByLoginIdentifier("jane@example.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("plaintext-pw", "hashed-pw")).thenReturn(true);

            UserResponse response = userService.login(request);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getLoginIdentifier()).isEqualTo("jane@example.com");
        }

        @Test
        @DisplayName("rejects an unknown login identifier")
        void login_unknownIdentifier_throws() {
            LoginRequest request = new LoginRequest("nobody@example.com", "pw");
            when(userRepo.findByLoginIdentifier("nobody@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("rejects a wrong password")
        void login_wrongPassword_throws() {
            LoginRequest request = new LoginRequest("jane@example.com", "wrong-pw");
            when(userRepo.findByLoginIdentifier("jane@example.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("wrong-pw", "hashed-pw")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("updates the hash when the old password matches")
        void changePassword_success() {
            ChangePasswordRequest request = new ChangePasswordRequest("old-pw", "new-pw");
            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("old-pw", "hashed-pw")).thenReturn(true);
            when(passwordEncoder.encode("new-pw")).thenReturn("new-hashed");

            userService.changePassword(1L, request);

            assertThat(existingUser.getPasswordHash()).isEqualTo("new-hashed");
            verify(userRepo).save(existingUser);
        }

        @Test
        @DisplayName("throws when the user does not exist")
        void changePassword_userNotFound_throws() {
            ChangePasswordRequest request = new ChangePasswordRequest("old-pw", "new-pw");
            when(userRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the old password is wrong")
        void changePassword_wrongOldPassword_throws() {
            ChangePasswordRequest request = new ChangePasswordRequest("wrong-old", "new-pw");
            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("wrong-old", "hashed-pw")).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1L, request))
                    .isInstanceOf(InvalidCredentialsException.class);
            verify(userRepo, never()).save(any());
        }

        @Test
        @DisplayName("rejects a blank new password")
        void changePassword_blankNewPassword_throws() {
            ChangePasswordRequest request = new ChangePasswordRequest("old-pw", "  ");
            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("old-pw", "hashed-pw")).thenReturn(true);

            assertThatThrownBy(() -> userService.changePassword(1L, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns the mapped response when found")
        void getUserById_success() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));

            UserResponse response = userService.getUserById(1L);

            assertThat(response.getLoginIdentifier()).isEqualTo("jane@example.com");
            assertThat(response.getRole()).isEqualTo(RoleEnum.USER);
        }

        @Test
        @DisplayName("throws when not found")
        void getUserById_notFound_throws() {
            when(userRepo.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(404L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
