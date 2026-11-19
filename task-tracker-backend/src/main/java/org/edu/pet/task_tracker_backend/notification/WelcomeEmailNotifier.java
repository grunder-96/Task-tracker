package org.edu.pet.task_tracker_backend.notification;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.EmailMessage;
import org.edu.pet.task_tracker_backend.service.EmailMessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WelcomeEmailNotifier {

    private static final String SUBJECT = "Welcome to TaskTracker!";

    private static final String BODY = """
            You have successfully registered in TaskTracker.
            You can now create tasks and track their progress.
            """;

    private final EmailMessagePublisher publisher;

    public void sendWelcome(Long userId, String email) {
        publisher.publish(userId, new EmailMessage(email, SUBJECT, BODY));
    }
}