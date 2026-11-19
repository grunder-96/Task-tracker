package org.edu.pet.task_tracker_scheduler.exception;

public class SummarizationServiceUnavailableException extends RuntimeException {

    public SummarizationServiceUnavailableException(String message) {
        super(message);
    }

    public SummarizationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}