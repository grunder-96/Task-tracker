package org.edu.pet.task_tracker_backend.event;

public record EmailSendingEvent(String to, String subject, String body) {

}