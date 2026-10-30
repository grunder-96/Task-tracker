package org.edu.pet.task_tracker_backend.exception;

public class UserTaskNotFoundException extends RuntimeException {

    public UserTaskNotFoundException(String message) {
        super(message);
    }

    public UserTaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}