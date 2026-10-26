package org.edu.pet.task_tracker_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Duration accessTokenTtl;

    public String generateToken(Long userId, String email) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .claim("id", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Long getUserId(String token) {

        return getPayload(token)
                .get("id",  Long.class);
    }

    public String getEmail(String token) {

        return getPayload(token)
                .getSubject();
    }

    private Claims getPayload(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}