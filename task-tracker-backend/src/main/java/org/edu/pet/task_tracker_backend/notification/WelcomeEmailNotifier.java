package org.edu.pet.task_tracker_backend.notification;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.event.EmailEventPublisher;
import org.edu.pet.task_tracker_backend.event.EmailSendingEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WelcomeEmailNotifier {

    private static final String SUBJECT = "Welcome to TaskTracker!";

    private static final String BODY = """
            You have successfully registered in TaskTracker.
            You can now create tasks and track their progress.
            """;

    private final EmailEventPublisher publisher;

    public void sendWelcome(String email) {
        publisher.publish(new EmailSendingEvent(email, SUBJECT, BODY));
    }
}