package org.edu.pet.task_tracker_summarization_server.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationRequest;
import org.edu.pet.task_tracker_summarization_server.dto.SummarizationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final String DLT_TOPIC_SUFFIX = "-dlt";

    @Bean
    public NewTopic summarizationRequestsTopic(@Value("${app.kafka.summarization.requests-topic-name}") String topicName) {

        return TopicBuilder
                .name(topicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic summarizationRequestsDltTopic(@Value("${app.kafka.summarization.requests-topic-name}") String topicName) {

        return TopicBuilder
                .name(topicName + DLT_TOPIC_SUFFIX)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic summarizationResponsesTopic(@Value("${app.kafka.summarization.responses-topic-name}") String topicName) {

        return TopicBuilder
                .name(topicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        configurer.configure(factory, consumerFactory);
        factory.setReplyTemplate(kafkaTemplate);

        return factory;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {

        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new DelegatingByTypeSerializer(Map.of(
                        byte[].class, new ByteArraySerializer(),
                        SummarizationRequest.class, new JacksonJsonSerializer<>(),
                        SummarizationResponse.class, new JacksonJsonSerializer<>()))
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                                   @Qualifier("summarizationRequestsDltTopic") NewTopic dltTopic) {

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2);
        backOff.setMaxInterval(4000L);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(dltTopic.name(), record.partition()));

        return new DefaultErrorHandler(recoverer, backOff);
    }
}