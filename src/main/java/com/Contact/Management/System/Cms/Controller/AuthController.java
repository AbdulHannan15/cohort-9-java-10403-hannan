package com.Contact.Management.System.Cms.Controller;

import com.Contact.Management.System.Cms.DTO.AuthResponse;
import com.Contact.Management.System.Cms.DTO.LoginRequest;
import com.Contact.Management.System.Cms.DTO.RegisterRequest;
import com.Contact.Management.System.Cms.DTO.UserResponse;
import com.Contact.Management.System.Cms.Exception.InvalidCredentialsException;
import com.Contact.Management.System.Cms.Security.CustomUserDetails;
import com.Contact.Management.System.Cms.Security.JwtService;
import com.Contact.Management.System.Cms.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginIdentifier(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials");
            throw new InvalidCredentialsException("Invalid login credentials");
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(principal, principal.getUserId());
        log.info("Issued JWT for userId={}", principal.getUserId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(principal.getUserId())
                .loginIdentifier(principal.getUsername())
                .role(principal.getUser().getRole())
                .build();

        return ResponseEntity.ok(response);
    }
}
