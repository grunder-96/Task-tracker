package org.edu.pet.task_tracker_backend.security;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.config.JwtProperties;
import org.edu.pet.task_tracker_backend.entity.RefreshTokenEntity;
import org.edu.pet.task_tracker_backend.exception.InvalidRefreshTokenException;
import org.edu.pet.task_tracker_backend.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String ERROR_MESSAGE = "Invalid refresh token";
    private static final int TOKEN_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties  jwtProperties;

    @Transactional
    public String generateToken(Long userId) {

        byte[] randomBytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .userId(userId)
                .expiresAt(Instant.now().plus(jwtProperties.refreshTtl()))
                .hash(hash(rawToken))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return rawToken;
    }

    // get userId and revoke current token
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Long getUserId(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token must not be null or empty");
        }

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException(ERROR_MESSAGE));

        if (refreshToken.isRevoked()) {

            refreshTokenRepository.revokeAllByUserId(refreshToken.getUserId());
            throw new InvalidRefreshTokenException(ERROR_MESSAGE);
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw  new InvalidRefreshTokenException(ERROR_MESSAGE);
        }

        int count = refreshTokenRepository.revokeByHash(refreshToken.getHash());

        if (count == 0) {
            throw new InvalidRefreshTokenException(ERROR_MESSAGE);
        }

        return refreshToken.getUserId();
    }

    @Transactional
    public void revoke(String rawRefreshToken) {

        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.revokeByHash(hash(rawRefreshToken));
        }
    }

    private String hash(String rawToken) {

        try {

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(md.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}