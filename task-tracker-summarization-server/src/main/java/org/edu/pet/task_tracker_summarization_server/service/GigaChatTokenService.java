package org.edu.pet.task_tracker_summarization_server.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.edu.pet.task_tracker_summarization_server.config.LlmProperties;
import org.edu.pet.task_tracker_summarization_server.exception.LlmServerUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class GigaChatTokenService implements LlmTokenProvider {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final Duration REFRESH_MARGIN = Duration.ofMinutes(3);
    private static final String SCOPE = "GIGACHAT_API_PERS";

    private final RestClient restClient;
    private final LlmProperties llmProperties;

    private String token;
    private Instant refreshAt = Instant.EPOCH;

    public GigaChatTokenService(@Qualifier("llmAuthRestClient") RestClient restClient, LlmProperties llmProperties) {

        this.llmProperties = llmProperties;
        this.restClient = restClient;
    }

    @Override
    public synchronized String getToken() {

        if (token == null || Instant.now().isAfter(refreshAt)) {
            refresh();
        }

        return token;
    }

    @Override
    public synchronized void invalidate() {
        this.token = null;
    }

    private void refresh() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("scope", SCOPE);

        TokenResponse tokenResponse = restClient.post()
            .uri(llmProperties.authUrl())
            .headers(h -> {
                h.setBasicAuth(llmProperties.authKey());
                h.set("RqUID", UUID.randomUUID().toString());
            })
            .body(form)
            .retrieve()
            .body(TokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new LlmServerUnavailableException(HttpStatus.OK.value(), "LLM returned no access token");
        }

        Instant expiry = tokenResponse.expiresAt() != null
                ? Instant.ofEpochMilli(tokenResponse.expiresAt())
                : Instant.now().plus(DEFAULT_TTL);

        token = tokenResponse.accessToken();
        refreshAt = expiry.minus(REFRESH_MARGIN);
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("expires_at") Long expiresAt) {

    }
}