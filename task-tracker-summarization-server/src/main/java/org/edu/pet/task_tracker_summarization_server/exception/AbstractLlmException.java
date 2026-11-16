package org.edu.pet.task_tracker_summarization_server.exception;

public abstract class AbstractLlmException extends RuntimeException {

    protected final int statusCode;

    public AbstractLlmException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public AbstractLlmException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}