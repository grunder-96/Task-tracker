package org.edu.pet.task_tracker_backend.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.edu.pet.task_tracker_backend.dto.LoginRequest;
import org.edu.pet.task_tracker_backend.dto.RegisterRequest;
import org.edu.pet.task_tracker_backend.dto.UserInfoResponse;
import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.edu.pet.task_tracker_backend.security.CustomUserDetails;
import org.edu.pet.task_tracker_backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    public AuthTokensResponse register(RegisterRequest registerRequest) {

        UserEntity userEntity = buildFrom(registerRequest);

        UserInfoResponse userInfoResponse = userService.create(userEntity);
        String accessToken = tokenProvider.generateToken(userInfoResponse.id(), userInfoResponse.email());
        return new AuthTokensResponse(accessToken);

    }

    public AuthTokensResponse login(LoginRequest loginRequest) {

        Authentication authenticate = authenticationManager.authenticate(UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequest.email(), loginRequest.password()));

        CustomUserDetails customUserDetails = (CustomUserDetails) authenticate.getPrincipal();

        String accessToken = tokenProvider.generateToken(
                customUserDetails.getId(),  customUserDetails.getUsername());

        return new AuthTokensResponse(accessToken);
    }

    private UserEntity buildFrom(RegisterRequest registerRequest) {

        return UserEntity.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .build();
    }
}