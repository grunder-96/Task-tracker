package org.edu.pet.task_tracker_backend.controller;

import com.sun.net.httpserver.Headers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.LoginRequest;
import org.edu.pet.task_tracker_backend.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final AuthResponseFactory authResponseFactory;
    private final RefreshCookieFactory refreshCookieFactory;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest loginRequest) {
        return authResponseFactory.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(
            name = RefreshCookieFactory.COOKIE_NAME, required = false) String rawRefreshToken) {
        return authResponseFactory.ok(authService.refresh(rawRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(
            name = RefreshCookieFactory.COOKIE_NAME, required = false) String rawRefreshToken) {

        authService.logout(rawRefreshToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.clear().toString())
                .build();
    }
}