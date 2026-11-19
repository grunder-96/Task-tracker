package org.edu.pet.task_tracker_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_backend.dto.EmailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailMessagePublisher {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;

    @Value("${app.kafka.email-sending-topic-name}")
    private String topic;

    public void publish(Long userId, EmailMessage emailMessage) {

        try {

            kafkaTemplate
                .send(topic, emailMessage)
                .whenComplete((res, ex) -> {

                    if (ex != null) {
                        log.warn("Email message for user {} was not delivered to Kafka", userId, ex);
                    }
            });
        } catch (RuntimeException e) {
            log.warn("Email message for user {} was not sent to Kafka", userId, e);
        }
    }
}