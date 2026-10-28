package org.edu.pet.task_tracker_backend.controller;

import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.edu.pet.task_tracker_backend.entity.RefreshTokenEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.client.ExchangeResult;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerIT extends AbstractIntegrationTest {

    @Test
    void givenValidLoginRequest_whenLogin_thenRefreshCookieIsSetAndOneMoreHashIsStored() {
        //given
        String email = "test@gmail.com";
        AuthTokensResponse authTokensResponse = registerAndGetTokens(email);
        //when
        ExchangeResult exchangeResult = loginRaw(email, PASSWORD)
        //then
                .expectAll(

                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectBody().isEmpty()
                )
                .returnResult();

        assertThat(exchangeResult.getResponseHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .startsWith("Bearer ");

        ResponseCookie tokenCookie = exchangeResult.getResponseCookies().getFirst("refresh_token");
        assertThat(tokenCookie).isNotNull();
        assertThat(tokenCookie.isHttpOnly()).isTrue();
        assertThat(tokenCookie.getPath()).isEqualTo("/auth");
        String rawToken = tokenCookie.getValue();

        assertThat(rawToken).isNotEqualTo(authTokensResponse.refreshToken());
        assertThat(refreshTokenRepository.count()).isEqualTo(2);

        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByHash(sha256Hex(rawToken))
                .orElseThrow();

        assertThat(tokenEntity.isRevoked()).isFalse();
        assertThat(tokenEntity.getHash()).isNotEqualTo(rawToken);
    }

    @ParameterizedTest
    @ValueSource(strings = {" test@gmail.com ", "TEST@gmail.com", "Test@Gmail.COM"})
    void givenSameEmailVariants_whenLogin_thenSuccess(String email) {
        //given
        registerAndGetTokens("test@gmail.com");
        long count = refreshTokenRepository.count();
        //when
        loginRaw(email, PASSWORD)
        //then
                .expectStatus().isOk();

        assertThat(refreshTokenRepository.count()).isEqualTo(count + 1);
    }

    @Test
    void givenNonExistentEmail_whenLogin_thenUnauthorizedAndNothingNewStored() {
        //given
        String nonExistentEmail = "email@gmail.com";
        long count = refreshTokenRepository.count();
        //when
        loginRaw(nonExistentEmail, PASSWORD)
        //then
                .expectStatus().isUnauthorized()
                .expectBody(String.class)
                .consumeWith(result ->
                        assertThat(result.getResponseBody()).contains("Invalid email or password"));

        assertThat(refreshTokenRepository.count()).isEqualTo(count);
    }

    @Test
    void givenExistingEmailAndWrongPassword_whenLogin_thenUnauthorizedAndNothingNewStored() {
        //given
        String email = "email@mail.ru";
        String wrongPassword = "ytrewq54332!";

        registerAndGetTokens(email);

        long count = refreshTokenRepository.count();
        //when
        loginRaw(email, wrongPassword)
        //then
                .expectStatus().isUnauthorized()
                .expectBody(String.class)
                .consumeWith(result ->
                        assertThat(result.getResponseBody()).contains("Invalid email or password"));

        assertThat(refreshTokenRepository.count()).isEqualTo(count);
    }

    @Test
    void givenValidRefreshToken_whenRefresh_thenTokenRotatedAndNewAccessTokenWorks() {
        //given
        String email = "test@gmail.com";
        AuthTokensResponse regTokensResponse = registerAndGetTokens(email);
        //when
        AuthTokensResponse refreshTokensResponse = refreshAndGetTokens(regTokensResponse.refreshToken());
        //then
        assertThat(refreshTokensResponse.refreshToken()).isNotEqualTo(regTokensResponse.refreshToken());
        assertThat(refreshTokenRepository.count()).isEqualTo(2);
        assertThat(refreshTokenRepository.findByHash(sha256Hex(regTokensResponse.refreshToken()))
                .orElseThrow().isRevoked()).isTrue();
        assertThat(refreshTokenRepository.findByHash(sha256Hex(refreshTokensResponse.refreshToken()))
                .orElseThrow().isRevoked()).isFalse();

        getUserRaw(refreshTokensResponse.accessToken())
                .expectStatus().isOk()
                .expectBody().jsonPath("$.email").isEqualTo(email);
    }

    @Test
    void givenRevokedRefreshToken_whenRefresh_thenAllUserRefreshTokensAreRevoked() {
        //given
        String email = "test@gmail.com";
        AuthTokensResponse registration = registerAndGetTokens(email);
        AuthTokensResponse login = loginAndGetTokens(email);
        AuthTokensResponse refresh = refreshAndGetTokens(registration.refreshToken());
        //when
        refreshRaw(registration.refreshToken())
                .expectStatus().isUnauthorized();
        //then
        refreshRaw(refresh.refreshToken()).expectStatus().isUnauthorized();
        refreshRaw(login.refreshToken()).expectStatus().isUnauthorized();
    }

    @Test
    void givenNoCookie_whenRefresh_thenUnauthorized() {
        //given

        //when
        restTestClient.post()
                .uri("/auth/refresh")
                .exchange()
        //then
                .expectStatus().isUnauthorized();
    }

    @Test
    void givenValidRefreshToken_whenLogout_thenCookieClearedAndTokenIsRevoked() {
        //given
        AuthTokensResponse tokens = registerAndGetTokens("test@gmail.com");
        //when
        ExchangeResult result = logoutRaw(tokens.refreshToken())
        //then
                .expectStatus().isNoContent()
                .returnResult();

        ResponseCookie clearedCookie = result.getResponseCookies().getFirst("refresh_token");
        assertThat(clearedCookie).isNotNull();
        assertThat(clearedCookie.getMaxAge()).isEqualTo(Duration.ZERO);
        assertThat(clearedCookie.getPath()).isEqualTo("/auth");

        refreshRaw(tokens.refreshToken()).expectStatus().isUnauthorized();
    }
}