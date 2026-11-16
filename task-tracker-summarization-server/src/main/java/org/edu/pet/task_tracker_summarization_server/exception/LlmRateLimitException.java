package org.edu.pet.task_tracker_summarization_server.exception;

public class LlmRateLimitException extends AbstractLlmException {

    public LlmRateLimitException(int statusCode, String message) {
        super(statusCode, message);
    }

    public LlmRateLimitException(int statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}