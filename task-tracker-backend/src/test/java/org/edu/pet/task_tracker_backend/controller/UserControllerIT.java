package org.edu.pet.task_tracker_backend.controller;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.edu.pet.task_tracker_backend.dto.UserInfoResponse;
import org.edu.pet.task_tracker_backend.entity.RefreshTokenEntity;
import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.edu.pet.task_tracker_backend.security.JwtAccessTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.ExchangeResult;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void givenValidRegisterRequest_whenRegister_thenRefreshCookieIsSetAndOnlyItsHashIsStored() {
        //given
        String email = "test@gmail.com";
        //when
        ExchangeResult exchangeResult = registerRaw(email, PASSWORD)
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

        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByHash(sha256Hex(rawToken))
                .orElseThrow();

        assertThat(tokenEntity.isRevoked()).isFalse();
        assertThat(tokenEntity.getHash()).isNotEqualTo(rawToken);
    }

    @ParameterizedTest
    @ValueSource(strings = {" test@gmail.com ", "TEST@gmail.com", "Test@Gmail.COM"})
    void givenExistingEmail_whenRegister_thenConflictAndNothingNewStored(String duplicate) {
        //given
        registerAndGetTokens("test@gmail.com");
        //when
        registerRaw(duplicate, PASSWORD)
        //then
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
        .expectBody().jsonPath("$.message").isNotEmpty();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    static Stream<Arguments> invalidRequests() {

        return Stream.of(

                Arguments.of(null, PASSWORD),
                Arguments.of("", PASSWORD),
                Arguments.of("   ", PASSWORD),
                Arguments.of("not-an-email", PASSWORD),
                Arguments.of("a".repeat(250) + "@mail.com", PASSWORD),
                Arguments.of("test@gmail.com", null),
                Arguments.of("test@gmail.com", ""),
                Arguments.of("test@gmail.com", "short1!"));
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void givenInvalidRequest_whenRegister_thenBadRequestAndNothingStored(String email, String password) {
        //given

        //when
        registerRaw(email, password)
        //then
            .expectStatus().isBadRequest()
            .expectBody().jsonPath("$.message").isNotEmpty();

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void givenEmptyBody_whenRegister_thenBadRequest() {
        //given

        //when
        restTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
        //then
                .expectStatus().isBadRequest();
    }

    @Test
    void givenValidRequest_whenRegister_thenHashedPasswordIsStored() {
        //given
        String email = "test@gmail.com";
        //when
        registerAndGetTokens(email);
        //then
        Optional<UserEntity> maybeStored = userRepository.findByEmailIgnoreCase(email);

        assertThat(maybeStored).isPresent();
        UserEntity stored = maybeStored.get();

        assertThat(stored.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, stored.getPassword())).isTrue();
    }

    @Test
    void givenAccessToken_whenGetUser_thenReturnsCurrentUser() {
        //given
        String email = "email@gmail.com";
        AuthTokensResponse tokens = registerAndGetTokens(email);
        //when
        UserInfoResponse userInfoResponse = restTestClient.get().uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .exchange()
        //then
                .expectStatus().isOk()
                .expectBody(UserInfoResponse.class)
                .returnResult()
                .getResponseBody();

        UserEntity userEntity = userRepository.findByEmailIgnoreCase(email).orElseThrow();

        assertThat(userInfoResponse)
                .isEqualTo(new UserInfoResponse(userEntity.getId(), userEntity.getEmail()));
    }

    @Test
    void givenNoToken_whenGetUser_thenUnauthorized() {
        //given

        //when
        restTestClient.get().uri("/user")
                .exchange()
        //then
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.message").isNotEmpty();
    }

    @Test
    void givenGarbageToken_whenGetUser_thenUnauthorized() {
        //given

        //when
        restTestClient.get().uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
                .exchange()
        //then
                .expectStatus().isUnauthorized();
    }

    @Test
    void givenExpiredToken_whenGetUser_thenUnauthorized() {
        //given
        String email = "test@gmail.com";
        UserEntity user = userRepository.save(
            new UserEntity(null, email, passwordEncoder.encode(PASSWORD)));

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
        String expiredToken = new JwtAccessTokenProvider(key, Duration.ofMinutes(-1))
                .generateToken(user.getId(), user.getEmail());
        //when
        restTestClient.get()
                .uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .exchange()
        //then
                .expectStatus().isUnauthorized();
    }

    @Test
    void givenValidTokenButUserDeleted_whenGetUser_thenUnauthorized() {
        //given
        AuthTokensResponse tokens = registerAndGetTokens("test@gmail.com");
        userRepository.deleteAll();
        //when
        restTestClient.get()
                .uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .exchange()
        //then
                .expectStatus().isUnauthorized();
    }
}