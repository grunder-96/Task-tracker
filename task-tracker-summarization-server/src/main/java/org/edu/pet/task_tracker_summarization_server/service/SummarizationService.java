package org.edu.pet.task_tracker_summarization_server.service;

import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_summarization_server.config.LlmProperties;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationRequest;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationResponse;
import org.edu.pet.task_tracker_summarization_server.exception.LlmServerUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
public class SummarizationService {

    private static final String SYSTEM_PROMPT = """
            Мне нужна помощь в составлении отчёта для пользователя о его задачах.
            
            Входные данные: json-массив с задачами пользователя, которые он выполнил за последние 24 часа(поле done со значением true), \
            а также с теми, что не выполнены за всё время(поле done со значением false).
            
            Правила:
            1. Отчёт - это обычный текст. Не используй markdown и звёздочки, решётки, обратные кавычки, таблицы, эмодзи. Список оформляй строками, которые начинаются с дефиса.
            2. Количество задач пиши цифрой и правильно склоняй слово «задача»: 1 задача, 2 задачи, 5 задач.
            3. Если выполненных или невыполненных задач нет, пропусти этот раздел. Если задач нет вообще, напиши об этом одной фразой.
            
            Пример ответа:
            За последние 24 часа вы выполнили 2 задачи:
            - Написать тесты
            - Отправить отчёт
            
            Остаётся невыполненной 1 задача:
            - Деплой
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Json-массив с задачами пользователя: %s
            """;

    private final RestClient restClient;
    private final JsonMapper mapper;
    private final LlmProperties llmProperties;
    private final ReportStorage reportStorage;

    public SummarizationService(@Qualifier("llmRequestClient") RestClient restClient,
                                JsonMapper mapper,
                                LlmProperties llmProperties,
                                ReportStorage reportStorage) {

        this.restClient = restClient;
        this.mapper = mapper;
        this.llmProperties = llmProperties;
        this.reportStorage = reportStorage;
    }

    public SummarizationResponse getResponse(SummarizationRequest request) {

        UUID requestId = request.requestId();

        String report = reportStorage.find(requestId)
                .orElseGet(() -> {

                    String generated = requestReport(request);
                    log.info("Report generated for request {}", requestId);

                    reportStorage.save(requestId, generated);

                    return generated;
                });

        return SummarizationResponse.success(report);
    }

    private String requestReport(SummarizationRequest request) {

        Map<String, Object> body = new HashMap<>();

        body.put("model", llmProperties.modelName());

        String userPrompt = USER_PROMPT_TEMPLATE.formatted(mapper.writeValueAsString(request.tasks()));

        body.put("messages", List.of(Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)));

        String rawString = restClient.post()
                .uri("/v1/chat/completions")
                .body(body)
                .retrieve()
                .body(String.class);

        String answer = (rawString == null || rawString.isBlank()) ?
            null : mapper.readTree(rawString)
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asString();

        if (answer == null || answer.isBlank()) {
            throw new LlmServerUnavailableException(HttpStatus.OK.value(), "LLM returned an empty answer");
        }

        return answer;
    }
}