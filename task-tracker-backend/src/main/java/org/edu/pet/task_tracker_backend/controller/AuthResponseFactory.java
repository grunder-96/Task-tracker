package org.edu.pet.task_tracker_backend.controller;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthResponseFactory {

    private final RefreshCookieFactory refreshCookieFactory;

    public ResponseEntity<Void> ok(AuthTokensResponse authTokensResponse) {

        return ResponseEntity.ok()
                .headers(buildAuthHeaders(authTokensResponse))
                .build();
    }

    private HttpHeaders buildAuthHeaders(AuthTokensResponse authTokensResponse) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(authTokensResponse.accessToken());
        headers.add(HttpHeaders.SET_COOKIE,
                refreshCookieFactory.create(authTokensResponse.refreshToken()).toString());

        return headers;
    }
}