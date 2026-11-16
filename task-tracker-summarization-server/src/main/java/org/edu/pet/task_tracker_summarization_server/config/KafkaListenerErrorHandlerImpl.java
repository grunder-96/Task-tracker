package org.edu.pet.task_tracker_summarization_server.config;

import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationRequest;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationResponse;
import org.edu.pet.task_tracker_summarization_server.exception.AbstractLlmException;
import org.edu.pet.task_tracker_summarization_server.exception.LlmAuthException;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
@Slf4j
public class KafkaListenerErrorHandlerImpl implements KafkaListenerErrorHandler {

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {

        String requestId = extractRequestId(message);
        Throwable cause = exception.getCause();

        switch (cause) {
            case LlmAuthException authException ->
                    log.error("LLM authentication failed for request {}: {}", requestId, authException.getMessage());
            case AbstractLlmException llmException ->
                    log.warn("Llm error(status: {}) for request {}: {}", llmException.getStatusCode(), requestId, llmException.getMessage());
            case ResourceAccessException accessException ->
                    log.warn("LLM is unreachable for request {}: {}", requestId, accessException.getMessage());
            case null, default ->
                    log.error("Unexpected error while building report for request {}", requestId, exception);
        }

        return SummarizationResponse.failed("Failed to generate report");
    }

    private String extractRequestId(Message<?> message) {

        if (message.getPayload() instanceof SummarizationRequest request) {
            return String.valueOf(request.requestId());
        }

        return "unknown";
    }
}