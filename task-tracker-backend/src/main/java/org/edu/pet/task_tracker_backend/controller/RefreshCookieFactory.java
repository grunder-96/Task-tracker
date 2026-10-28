package org.edu.pet.task_tracker_backend.controller;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.config.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshCookieFactory {

    private static final String EMPTY_VALUE = "";
    public static final String COOKIE_NAME = "refresh_token";

    private final JwtProperties jwtProperties;

    public ResponseCookie create(String refreshToken) {

        return base(refreshToken)
                .maxAge(jwtProperties.refreshTtl())
                .build();
    }

    public ResponseCookie clear() {

        return base(EMPTY_VALUE)
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {

        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(jwtProperties.refreshCookieSecure())
                .sameSite("Lax")
                .path("/auth");
    }
}