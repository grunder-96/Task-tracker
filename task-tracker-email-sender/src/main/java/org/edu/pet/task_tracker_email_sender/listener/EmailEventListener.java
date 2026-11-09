package org.edu.pet.task_tracker_email_sender.listener;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_email_sender.event.EmailSendingEvent;
import org.edu.pet.task_tracker_email_sender.service.EmailSendingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailSendingService emailSendingService;

    @KafkaListener(topics = "${app.kafka.email-sending-topic-name}")
    public void listen(EmailSendingEvent event) {
        emailSendingService.sendEmail(event);
    }
}