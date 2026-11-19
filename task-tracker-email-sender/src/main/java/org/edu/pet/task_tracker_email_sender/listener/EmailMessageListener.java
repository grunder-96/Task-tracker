package org.edu.pet.task_tracker_email_sender.listener;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_email_sender.dto.EmailMessage;
import org.edu.pet.task_tracker_email_sender.service.EmailSendingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailMessageListener {

    private final EmailSendingService emailSendingService;

    @KafkaListener(topics = "${app.kafka.email.sending-topic-name}")
    public void listen(EmailMessage emailMessage) {
        emailSendingService.sendEmail(emailMessage);
    }
}