package org.edu.pet.task_tracker_backend.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.edu.pet.task_tracker_backend.dto.LoginRequest;
import org.edu.pet.task_tracker_backend.dto.RegisterRequest;
import org.edu.pet.task_tracker_backend.dto.UserInfoResponse;
import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.edu.pet.task_tracker_backend.security.CustomUserDetails;
import org.edu.pet.task_tracker_backend.security.JwtAccessTokenProvider;
import org.edu.pet.task_tracker_backend.security.RefreshTokenService;
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

    private final JwtAccessTokenProvider accessTokenProvider;
    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    public AuthTokensResponse register(RegisterRequest registerRequest) {

        UserEntity userEntity = buildFrom(registerRequest);

        UserInfoResponse userInfoResponse = userService.create(userEntity);

        return prepareTokensResponse(userInfoResponse.id(), userInfoResponse.email());
    }

    public AuthTokensResponse login(LoginRequest loginRequest) {

        Authentication authenticate = authenticationManager.authenticate(UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequest.email(), loginRequest.password()));

        CustomUserDetails customUserDetails = (CustomUserDetails) authenticate.getPrincipal();

        return prepareTokensResponse(customUserDetails.getId(), customUserDetails.getUsername());
    }

    public AuthTokensResponse refresh(String rawRefreshToken) {

        Long userId = refreshTokenService.getUserId(rawRefreshToken);

        UserInfoResponse userInfoResponse = userService.getUserInfo(userId);

        return  prepareTokensResponse(userInfoResponse.id(), userInfoResponse.email());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthTokensResponse prepareTokensResponse(Long userId, String email) {

        String accessToken = accessTokenProvider.generateToken(userId, email);
        String refreshToken = refreshTokenService.generateToken(userId);

        return new AuthTokensResponse(accessToken, refreshToken);
    }

    private UserEntity buildFrom(RegisterRequest registerRequest) {

        return UserEntity.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .build();
    }
}