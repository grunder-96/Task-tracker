package org.edu.pet.task_tracker_backend.exception;

public class UserAlreadyTakenException extends RuntimeException {

    public UserAlreadyTakenException(String message) {
        super(message);
    }

    public UserAlreadyTakenException(String message, Throwable cause) {
        super(message, cause);
    }
}