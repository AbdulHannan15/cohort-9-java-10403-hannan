package com.Contact.Management.System.Cms.Controller;

import com.Contact.Management.System.Cms.DTO.ChangePasswordRequest;
import com.Contact.Management.System.Cms.DTO.UserResponse;
import com.Contact.Management.System.Cms.Security.CustomUserDetails;
import com.Contact.Management.System.Cms.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(userService.getUserById(principal.getUserId()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal CustomUserDetails principal,
                                                @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getUserId(), request);
        return ResponseEntity.noContent().build();
    }
}
