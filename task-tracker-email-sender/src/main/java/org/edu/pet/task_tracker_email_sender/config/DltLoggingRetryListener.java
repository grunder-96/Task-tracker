package org.edu.pet.task_tracker_email_sender.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DltLoggingRetryListener implements RetryListener {

    @Override
    public void failedDelivery(ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {

        log.warn("Delivery attempt {} failed: topic={}, offset={}, error={}",
                deliveryAttempt, record.topic(), record.offset(), ex.getClass().getSimpleName());
    }

    @Override
    public void recovered(ConsumerRecord<?, ?> record, Exception ex) {

        log.error("Record sent to DLT: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset(), ex);
    }

    @Override
    public void recoveryFailed(ConsumerRecord<?, ?> record, Exception original, Exception failure) {

        log.error("Failed to send record to DLT: topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset(), failure);
    }
}