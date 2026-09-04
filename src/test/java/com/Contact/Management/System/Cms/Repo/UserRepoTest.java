package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepo")
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    private User persistUser(String loginIdentifier, String recoveryPhone) {
        User user = User.builder()
                .loginIdentifier(loginIdentifier)
                .passwordHash("hashed-pw")
                .recoveryPhone(recoveryPhone)
                .role(RoleEnum.USER)
                .build();
        return userRepo.save(user);
    }

    @Test
    @DisplayName("findByLoginIdentifier returns the matching user")
    void findByLoginIdentifier_found() {
        persistUser("jane@example.com", "+15551234567");

        assertThat(userRepo.findByLoginIdentifier("jane@example.com"))
                .isPresent()
                .get()
                .extracting(User::getLoginIdentifier)
                .isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("findByLoginIdentifier is empty for an unknown identifier")
    void findByLoginIdentifier_notFound() {
        assertThat(userRepo.findByLoginIdentifier("nobody@example.com")).isEmpty();
    }

    @Test
    @DisplayName("existsByLoginIdentifier reflects saved state")
    void existsByLoginIdentifier() {
        persistUser("jane@example.com", null);

        assertThat(userRepo.existsByLoginIdentifier("jane@example.com")).isTrue();
        assertThat(userRepo.existsByLoginIdentifier("nobody@example.com")).isFalse();
    }

    @Test
    @DisplayName("existsByRecoveryPhone reflects saved state")
    void existsByRecoveryPhone() {
        persistUser("jane@example.com", "+15551234567");

        assertThat(userRepo.existsByRecoveryPhone("+15551234567")).isTrue();
        assertThat(userRepo.existsByRecoveryPhone("+19998887777")).isFalse();
    }

    @Test
    @DisplayName("loginIdentifier uniqueness is enforced at the DB level")
    void loginIdentifier_isUnique() {
        persistUser("jane@example.com", null);

        User duplicate = User.builder()
                .loginIdentifier("jane@example.com")
                .passwordHash("other-hash")
                .role(RoleEnum.USER)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> userRepo.saveAndFlush(duplicate)
        );
    }
}
