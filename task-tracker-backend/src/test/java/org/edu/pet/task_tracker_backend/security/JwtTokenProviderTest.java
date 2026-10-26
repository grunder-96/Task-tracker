package org.edu.pet.task_tracker_backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtTokenProviderTest {

    private static final SecretKey KEY = Jwts.SIG.HS256.key().build();
    private static JwtTokenProvider provider;

    @BeforeAll
    static void init() {
        provider = new JwtTokenProvider(KEY, Duration.ofMinutes(3));
    }

    @Test
    void givenId_whenGenerateToken_thenGetUserIdReturnsSameId() {
        //given
        Long id = 12L;
        String email = "test@gmail.com";
        //when
        String token = provider.generateToken(id, email);
        //then
        assertThat(provider.getUserId(token)).isEqualTo(id);
    }

    @Test
    void givenExpiredToken_whenGetUserId_thenThrowException() {
        //given
        JwtTokenProvider expiredProvider = new JwtTokenProvider(KEY, Duration.ofMinutes(-1));
        String expiredToken = expiredProvider.generateToken(12L, "test@gmail.com");
        //when

        //then
        assertThatThrownBy(() -> expiredProvider.getUserId(expiredToken)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void givenAnotherKeySignedToken_whenGetUserId_thenThrowException() {
        //given
        SecretKey anotherKey = Jwts.SIG.HS256.key().build();
        JwtTokenProvider anotherProvider = new JwtTokenProvider(anotherKey, Duration.ofMinutes(3));
        String anotherToken = anotherProvider.generateToken(12L, "test@gmail.com");
        //when

        //then
        assertThatThrownBy(() -> provider.getUserId(anotherToken)).isInstanceOf(SignatureException.class);
    }

    @Test
    void givenCompromisedToken_whenGetUserId_thenThrowException() {
        //given
        String token = provider.generateToken(12L, "test@gmail.com");
        String[] tokenParts = token.split("\\.");

        String anotherToken = provider.generateToken(13L, "test@gmail.com");
        String[] anotherTokenParts = anotherToken.split("\\.");

        String[] compromisedTokenParts = new String[tokenParts.length];
        compromisedTokenParts[0] = tokenParts[0];
        compromisedTokenParts[1] = anotherTokenParts[1];
        compromisedTokenParts[2] = tokenParts[2];
        String compromisedToken = String.join(".", compromisedTokenParts);
        //when

        //then
        assertThatThrownBy(() -> provider.getUserId(compromisedToken)).isInstanceOf(SignatureException.class);
    }
}