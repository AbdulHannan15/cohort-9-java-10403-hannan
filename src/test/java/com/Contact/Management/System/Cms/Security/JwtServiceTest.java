package com.Contact.Management.System.Cms.Security;

import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // @Value fields are only populated by Spring; set them directly for this plain unit test.
        ReflectionTestUtils.setField(jwtService, "secret", "unit-test-secret-unit-test-secret-32bytes");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);

        User user = User.builder()
                .id(7L)
                .loginIdentifier("jane@example.com")
                .passwordHash("hash")
                .role(RoleEnum.USER)
                .build();
        userDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("generates a token whose subject and userId claim round-trip correctly")
    void generateToken_roundTrips() {
        String token = jwtService.generateToken(userDetails, userDetails.getUserId());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("jane@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(7L);
    }

    @Test
    @DisplayName("isTokenValid is true for a fresh token belonging to the same user")
    void isTokenValid_true_forMatchingUser() {
        String token = jwtService.generateToken(userDetails, userDetails.getUserId());

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid is false when the token belongs to a different user")
    void isTokenValid_false_forDifferentUser() {
        String token = jwtService.generateToken(userDetails, userDetails.getUserId());

        User otherUser = User.builder()
                .id(8L)
                .loginIdentifier("someone-else@example.com")
                .passwordHash("hash")
                .role(RoleEnum.USER)
                .build();
        CustomUserDetails otherDetails = new CustomUserDetails(otherUser);

        assertThat(jwtService.isTokenValid(token, otherDetails)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid is false for an expired token")
    void isTokenValid_false_whenExpired() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L); // already expired
        String token = jwtService.generateToken(userDetails, userDetails.getUserId());

        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid is false for a garbage token")
    void isTokenValid_false_forMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-real-jwt", userDetails)).isFalse();
    }
}
