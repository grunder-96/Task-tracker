package org.edu.pet.task_tracker_summarization_server.listener;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationRequest;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationResponse;
import org.edu.pet.task_tracker_summarization_server.service.SummarizationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SummarizationListener {

    private final SummarizationService summarizationService;
    private final Validator validator;

    @KafkaListener(topics = "${app.kafka.summarization.requests-topic-name}", errorHandler = "kafkaListenerErrorHandlerImpl")
    @SendTo
    public SummarizationResponse listen(SummarizationRequest request) {

        Set<ConstraintViolation<SummarizationRequest>> constraintViolationSet = validator.validate(request);

        if (!constraintViolationSet.isEmpty()) {

            String message = constraintViolationSet.stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));

            return SummarizationResponse.failed("Invalid request: " + message);
        }

        return summarizationService.getResponse(request);
    }
}