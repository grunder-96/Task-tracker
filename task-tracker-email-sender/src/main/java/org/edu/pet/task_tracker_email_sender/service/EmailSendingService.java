package org.edu.pet.task_tracker_email_sender.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_email_sender.config.AppMailProperties;
import org.edu.pet.task_tracker_email_sender.event.EmailSendingEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendingService {

    private final JavaMailSender mailSender;
    private final AppMailProperties appMailProperties;

    public void sendEmail(EmailSendingEvent event) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(appMailProperties.from());
        mailMessage.setTo(event.to());
        mailMessage.setSubject(event.subject());
        mailMessage.setText(event.body());

        mailSender.send(mailMessage);
        log.info("Email sent to {}", maskEmail(event.to()));
    }

    private String maskEmail(String email) {

        final String mask = "***";

        int idx = email.indexOf('@');
        return idx <= 1 ? mask : email.charAt(0) + mask + email.substring(idx);
    }
}