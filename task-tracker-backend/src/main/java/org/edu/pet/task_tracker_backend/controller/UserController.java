package org.edu.pet.task_tracker_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.edu.pet.task_tracker_backend.dto.RegisterRequest;
import org.edu.pet.task_tracker_backend.dto.UserInfoResponse;
import org.edu.pet.task_tracker_backend.security.CustomUserDetails;
import org.edu.pet.task_tracker_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final AuthService authService;

    @GetMapping
    public UserInfoResponse getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new UserInfoResponse(customUserDetails.getId(), customUserDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest registerRequest) {

        AuthTokensResponse tokensResponse = authService.register(registerRequest);
        return ResponseEntity.ok()
                .headers(AuthHeaderUtils.buildAuthHeaders(tokensResponse))
                .build();
    }
}