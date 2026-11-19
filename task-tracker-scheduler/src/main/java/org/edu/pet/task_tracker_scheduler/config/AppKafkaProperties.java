package org.edu.pet.task_tracker_scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.kafka.summarization")
public record AppKafkaProperties(String requestsTopicName,
                                 String responsesTopicName,
                                 String emailSendingTopicName,
                                 Duration replyTimeout) {

}