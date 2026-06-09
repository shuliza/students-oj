package com.studentoj.auth.controller;

import com.studentoj.auth.dto.ChangePasswordRequest;
import com.studentoj.auth.dto.LoginRequest;
import com.studentoj.auth.dto.LoginResponse;
import com.studentoj.auth.dto.UpdateProfileRequest;
import com.studentoj.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        authService.logout(extractToken(request));
    }

    @GetMapping("/me")
    public LoginResponse me(HttpServletRequest request) {
        return authService.me(extractToken(request));
    }

    @PostMapping("/password")
    public void changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest body) {
        authService.changePassword(extractToken(request), body);
    }

    @PutMapping("/profile")
    public LoginResponse updateProfile(HttpServletRequest request, @RequestBody UpdateProfileRequest body) {
        return authService.updateProfile(extractToken(request), body);
    }

    @GetMapping("/introspect")
    public ResponseEntity<Void> introspect(HttpServletRequest request) {
        LoginResponse user = authService.me(extractToken(request));
        return ResponseEntity.noContent()
                .header("X-Auth-User-Id", String.valueOf(user.userId()))
                .header("X-Auth-Username", user.username())
                .header("X-Auth-User-Role", user.role())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        if (header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return header.trim();
    }
}
