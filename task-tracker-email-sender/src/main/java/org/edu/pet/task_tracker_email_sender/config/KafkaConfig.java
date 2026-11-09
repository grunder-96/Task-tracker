package org.edu.pet.task_tracker_email_sender.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.edu.pet.task_tracker_email_sender.event.EmailSendingEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;

import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final String DLT_TOPIC_SUFFIX = "-dlt";

    @Bean
    public NewTopic emailSendingTopic(@Value("${app.kafka.email-sending-topic-name}") String emailSendingTopicName) {

        return TopicBuilder
                .name(emailSendingTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailSendingDltTopic(@Value("${app.kafka.email-sending-topic-name}") String emailSendingTopicName) {

        return TopicBuilder
                .name(emailSendingTopicName + DLT_TOPIC_SUFFIX)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {

        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        return new DefaultKafkaProducerFactory<>(
            config,
            new StringSerializer(),
            new DelegatingByTypeSerializer(Map.of(
                byte[].class, new ByteArraySerializer(),
                EmailSendingEvent.class, new JacksonJsonSerializer<>()))
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                                   @Qualifier("emailSendingDltTopic") NewTopic dltTopic,
                                                   RetryListener retryListener) {

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2);
        backOff.setMaxInterval(4000L);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(dltTopic.name(), record.partition()));

        DefaultErrorHandler defaultErrorHandler = new  DefaultErrorHandler(recoverer, backOff);

        defaultErrorHandler.addNotRetryableExceptions(MailParseException.class, MailAuthenticationException.class);

        defaultErrorHandler.setRetryListeners(retryListener);

        return defaultErrorHandler;
    }
}