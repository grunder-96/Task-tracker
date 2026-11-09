package org.edu.pet.task_tracker_backend.listener;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.event.UserRegisteredEvent;
import org.edu.pet.task_tracker_backend.notification.WelcomeEmailNotifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WelcomeEmailListener {

    private final WelcomeEmailNotifier notifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserRegistered(UserRegisteredEvent event) {
        notifier.sendWelcome(event.email());
    }
}