package org.edu.pet.task_tracker_summarization_server.exception;

public class LlmAuthException extends AbstractLlmException {

    public LlmAuthException(int errorCode, String message) {
        super(errorCode, message);
    }

    public LlmAuthException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}