package org.edu.pet.task_tracker_backend.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailEventPublisher {

    private final KafkaTemplate<String, EmailSendingEvent> kafkaTemplate;

    @Value("${app.kafka.email-sending-topic-name}")
    private String topic;

    public void publish(EmailSendingEvent event) {

        try {

            kafkaTemplate
                .send(topic, event)
                .whenComplete((res, ex) -> {

                    if (ex != null) {
                        log.warn("Email event was not delivered to Kafka", ex);
                    }
            });
        } catch (RuntimeException e) {
            log.warn("Email event was not sent to Kafka", e);
        }
    }
}