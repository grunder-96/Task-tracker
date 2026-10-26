package org.edu.pet.task_tracker_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.edu.pet.task_tracker_backend.dto.LoginRequest;
import org.edu.pet.task_tracker_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest loginRequest) {

        AuthTokensResponse tokensResponse = authService.login(loginRequest);

        return ResponseEntity.ok()
                .headers(AuthHeaderUtils.buildAuthHeaders(tokensResponse))
                .build();
    }
}