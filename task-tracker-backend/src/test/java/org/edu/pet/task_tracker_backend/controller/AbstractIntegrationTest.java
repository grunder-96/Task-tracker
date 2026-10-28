package org.edu.pet.task_tracker_backend.controller;

import org.edu.pet.task_tracker_backend.config.DatabaseTestConfiguration;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.edu.pet.task_tracker_backend.dto.LoginRequest;
import org.edu.pet.task_tracker_backend.dto.RegisterRequest;
import org.edu.pet.task_tracker_backend.repository.RefreshTokenRepository;
import org.edu.pet.task_tracker_backend.repository.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DatabaseTestConfiguration.class)
@AutoConfigureRestTestClient
abstract class AbstractIntegrationTest {

    protected static final String JWT_SECRET = "xvRwGbjlQwE1bK7rl9aXqtkvkXmSTBSOmXsvLI5z3IQ=";
    protected static final String PASSWORD = "Qwe12345!";

    @Autowired
    protected RestTestClient restTestClient;

    @Autowired
    protected UserEntityRepository userRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("app.jwt.secret", () -> JWT_SECRET);
    }

    @BeforeEach
    void cleanUp() {
        // ON DELETE CASCADE(refresh_tokens)
        userRepository.deleteAll();
    }

    protected AuthTokensResponse registerAndGetTokens(String email) {

        ExchangeResult result = registerRaw(email, PASSWORD)
                .expectStatus().isOk()
                .returnResult();

        return extractTokens(result);
    }

    protected AuthTokensResponse loginAndGetTokens(String email) {

        ExchangeResult result = loginRaw(email, PASSWORD)
                .expectStatus().isOk()
                .returnResult();

        return extractTokens(result);
    }

    protected AuthTokensResponse refreshAndGetTokens(String rawRefreshToken) {

        ExchangeResult result = refreshRaw(rawRefreshToken)
                .expectStatus().isOk()
                .returnResult();

        return extractTokens(result);
    }

    protected RestTestClient.ResponseSpec refreshRaw(String rawRefreshToken) {

        return restTestClient.post()
                .uri("/auth/refresh")
                .cookie("refresh_token", rawRefreshToken)
                .exchange();
    }

    protected RestTestClient.ResponseSpec registerRaw(String email, String password) {

        return restTestClient.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest(email, password))
                .exchange();
    }

    protected RestTestClient.ResponseSpec loginRaw(String email, String password) {

        return restTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, password))
                .exchange();
    }

    protected RestTestClient.ResponseSpec logoutRaw(String rawRefreshToken) {

        return restTestClient.post()
                .uri("/auth/logout")
                .cookie("refresh_token", rawRefreshToken)
                .exchange();
    }

    protected RestTestClient.ResponseSpec getUserRaw(String rawAccessToken) {

        return restTestClient.get()
                .uri("/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + rawAccessToken)
                .exchange();
    }

    protected static String sha256Hex(String value) {

        try {

            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private AuthTokensResponse extractTokens(ExchangeResult result) {

        String bearer = result.getResponseHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assertThat(bearer).startsWith("Bearer ");
        String accessToken = bearer.substring("Bearer ".length());

        ResponseCookie cookie = result.getResponseCookies().getFirst("refresh_token");
        assertThat(cookie).isNotNull();
        String refreshToken = cookie.getValue();

        return new AuthTokensResponse(accessToken, refreshToken);
    }
}