package com.Contact.Management.System.Cms.Controller;

import com.Contact.Management.System.Cms.DTO.UserResponse;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.Security.CustomUserDetails;
import com.Contact.Management.System.Cms.Security.JwtService;
import com.Contact.Management.System.Cms.Service.UserService;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // security filters aren't under test here; the endpoint's own logic is
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/auth/register returns 200 with the created user")
    void register_returnsUser() throws Exception {
        String requestBody = """
                {"loginIdentifier":"jane@example.com","password":"password123","recoveryPhone":null}
                """;
        UserResponse response = UserResponse.builder()
                .id(1L).loginIdentifier("jane@example.com").role(RoleEnum.USER).build();
        when(userService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loginIdentifier").value("jane@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns a JWT on success")
    void login_success_returnsToken() throws Exception {
        String requestBody = """
                {"loginIdentifier":"jane@example.com","password":"password123"}
                """;

        User user = User.builder().id(1L).loginIdentifier("jane@example.com")
                .passwordHash("hash").role(RoleEnum.USER).build();
        CustomUserDetails principal = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(any(), any())).thenReturn("signed.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed.jwt.token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.loginIdentifier").value("jane@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 on bad credentials")
    void login_badCredentials_returns401() throws Exception {
        String requestBody = """
                {"loginIdentifier":"jane@example.com","password":"wrong-password"}
                """;
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid login credentials"));
    }
}
