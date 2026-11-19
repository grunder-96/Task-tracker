package org.edu.pet.task_tracker_email_sender.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_email_sender.config.AppMailProperties;
import org.edu.pet.task_tracker_email_sender.dto.EmailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendingService {

    private final JavaMailSender mailSender;
    private final AppMailProperties appMailProperties;

    public void sendEmail(EmailMessage emailMessage) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(appMailProperties.from());
        mailMessage.setTo(emailMessage.to());
        mailMessage.setSubject(emailMessage.subject());
        mailMessage.setText(emailMessage.body());

        mailSender.send(mailMessage);
        log.info("Email message sent to {}", maskEmail(emailMessage.to()));
    }

    private String maskEmail(String email) {

        final String mask = "***";

        int idx = email.indexOf('@');
        return idx <= 1 ? mask : email.charAt(0) + mask + email.substring(idx);
    }
}