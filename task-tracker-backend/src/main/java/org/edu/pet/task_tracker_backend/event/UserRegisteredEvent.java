package org.edu.pet.task_tracker_backend.event;

public record UserRegisteredEvent(Long userId, String email) {

}