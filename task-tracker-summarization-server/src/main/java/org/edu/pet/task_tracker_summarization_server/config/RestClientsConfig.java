package org.edu.pet.task_tracker_summarization_server.config;

import org.edu.pet.task_tracker_summarization_server.exception.LlmAuthException;
import org.edu.pet.task_tracker_summarization_server.exception.LlmBadRequestException;
import org.edu.pet.task_tracker_summarization_server.exception.LlmRateLimitException;
import org.edu.pet.task_tracker_summarization_server.exception.LlmServerUnavailableException;
import org.edu.pet.task_tracker_summarization_server.service.LlmTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class RestClientsConfig {

    @Bean
    public RestClient llmAuthRestClient(RestClient.Builder builder) {

        return withErrorHandling(builder)
            .defaultHeaders(h -> {
                h.setAccept(List.of(MediaType.APPLICATION_JSON));
                h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            })
            .build();
    }

    @Bean
    public RestClient llmRequestClient(RestClient.Builder builder,
                                       LlmProperties llmProperties,
                                       LlmTokenProvider llmTokenProvider) {

        return withErrorHandling(builder)
            .baseUrl(llmProperties.apiBaseUrl())
            .requestInterceptor((req, body, execution) -> {

                req.getHeaders().setBearerAuth(llmTokenProvider.getToken());

                ClientHttpResponse response = execution.execute(req, body);

                if (response.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {

                    response.close();
                    llmTokenProvider.invalidate();

                    String newToken = llmTokenProvider.getToken();

                    req.getHeaders().setBearerAuth(newToken);
                    return execution.execute(req, body);
                }

                return response;
            })
            .build();
    }

    private RestClient.Builder withErrorHandling(RestClient.Builder builder) {

        return builder
            .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, resp) -> {

                int statusCode = resp.getStatusCode().value();

                if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                    throw new LlmAuthException(statusCode, "Authentication failed");
                }

                if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
                    throw new LlmRateLimitException(statusCode, "LLM rate limit exceeded");
                }

                throw new LlmBadRequestException(statusCode, "Incorrect request format");
            })
            .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, resp) -> {
                throw new LlmServerUnavailableException(resp.getStatusCode().value(), "Llm server is unavailable");
            });
    }
}