package org.edu.pet.task_tracker_scheduler.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.edu.pet.task_tracker_scheduler.config.AppKafkaProperties;
import org.edu.pet.task_tracker_scheduler.dto.SummarizationRequest;
import org.edu.pet.task_tracker_scheduler.dto.SummarizationResponse;
import org.edu.pet.task_tracker_scheduler.exception.SummarizationServiceUnavailableException;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class KafkaSummarizationReplyService {

    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REPLY_TIMEOUT_MARGIN = Duration.ofSeconds(5);

    private final ReplyingKafkaTemplate<String, Object, SummarizationResponse> template;
    private final AppKafkaProperties appKafkaProperties;

    public SummarizationResponse getSummarizationReply(SummarizationRequest request) {

        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(appKafkaProperties.requestsTopicName(),
                request);

        RequestReplyFuture<String, Object, SummarizationResponse> replyFuture =
                template.sendAndReceive(producerRecord, appKafkaProperties.replyTimeout());

        try {

            replyFuture.getSendFuture().get(SEND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            Duration consumerRecordTimeout = appKafkaProperties.replyTimeout().plus(REPLY_TIMEOUT_MARGIN);

            ConsumerRecord<String, SummarizationResponse> consumerRecord =
                    replyFuture.get(consumerRecordTimeout.toMillis(), TimeUnit.MILLISECONDS);

            SummarizationResponse answer = consumerRecord.value();

            if (answer == null) {
                throw new SummarizationServiceUnavailableException("Empty reply for request " + request.requestId(), null);
            }

            return answer;
        } catch (InterruptedException e) {

            //restore interrupt status
            Thread.currentThread().interrupt();
            throw new SummarizationServiceUnavailableException("Interrupted while waiting reply for request " + request.requestId(), e);
        } catch (ExecutionException | TimeoutException e) {
            throw new SummarizationServiceUnavailableException("No reply for request " + request.requestId(), e);
        }
    }
}