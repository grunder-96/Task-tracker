package org.edu.pet.task_tracker_scheduler.config;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_scheduler.dto.SummarizationResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final AppKafkaProperties appKafkaProperties;

    @Bean
    public ConcurrentMessageListenerContainer<String, SummarizationResponse> replyContainer(ConcurrentKafkaListenerContainerFactory<String, SummarizationResponse> factory) {

        ConcurrentMessageListenerContainer<String, SummarizationResponse> container = factory.createContainer(appKafkaProperties.responsesTopicName());

        container.getContainerProperties().setGroupId("scheduler-replies");
        container.setAutoStartup(false);

        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, Object, SummarizationResponse> kafkaTemplate(ProducerFactory<String, Object> producerFactory,
                                                                                      GenericMessageListenerContainer<String, SummarizationResponse> container) {

        return new ReplyingKafkaTemplate<>(producerFactory, container);
    }
}