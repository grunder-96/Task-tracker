package org.edu.pet.task_tracker_scheduler.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_scheduler.config.AppKafkaProperties;
import org.edu.pet.task_tracker_scheduler.dto.EmailMessage;
import org.edu.pet.task_tracker_scheduler.exception.SummarizationServiceUnavailableException;
import org.edu.pet.task_tracker_scheduler.model.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class KafkaEmailSendingService {

    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(5);
    private static final String SUBJECT = "Task Tracker report";

    private final KafkaTemplate<String, Object> template;
    private final AppKafkaProperties appKafkaProperties;

    public void sendToKafka(User user, String report) {

        EmailMessage emailMessage = new EmailMessage(user.email(), SUBJECT, report);

        try {

            template.send(appKafkaProperties.emailSendingTopicName(), emailMessage)
                    .get(SEND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {

            //restore interrupt status
            Thread.currentThread().interrupt();
            throw new SummarizationServiceUnavailableException("Interrupted while sending email message for user " + user.id(), e);
        } catch (ExecutionException | TimeoutException e) {
            throw new SummarizationServiceUnavailableException("Email message for user " + user.id() + " was not delivered to Kafka", e);
        }
    }
}