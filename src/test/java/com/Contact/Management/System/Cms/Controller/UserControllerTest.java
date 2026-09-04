package com.Contact.Management.System.Cms.Controller;

import com.Contact.Management.System.Cms.DTO.UserResponse;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.Security.CustomUserDetails;
import com.Contact.Management.System.Cms.Service.UserService;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private CustomUserDetails principal() {
        User user = User.builder().id(1L).loginIdentifier("jane@example.com")
                .passwordHash("hash").role(RoleEnum.USER).build();
        return new CustomUserDetails(user);
    }

    @Test
    @DisplayName("GET /api/users/me returns the authenticated user's profile")
    void getCurrentUser_returnsProfile() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L).loginIdentifier("jane@example.com").role(RoleEnum.USER).build();
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/me").with(user(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loginIdentifier").value("jane@example.com"));
    }

    @Test
    @DisplayName("PUT /api/users/me/password changes the password for the authenticated user")
    void changePassword_returnsNoContent() throws Exception {
        String requestBody = """
                {"oldPassword":"old-pw","newPassword":"new-pw"}
                """;

        mockMvc.perform(put("/api/users/me/password")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq(1L), org.mockito.ArgumentMatchers.argThat(req ->
                "old-pw".equals(req.getOldPassword()) && "new-pw".equals(req.getNewPassword())
        ));
    }
}
