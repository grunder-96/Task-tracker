package org.edu.pet.task_tracker_summarization_server.exception;

public class LlmBadRequestException extends AbstractLlmException {

    public LlmBadRequestException(int errorCode, String message) {
        super(errorCode, message);
    }

    public LlmBadRequestException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}