package org.edu.pet.task_tracker_summarization_server.exception;

public class LlmServerUnavailableException extends AbstractLlmException{

    public LlmServerUnavailableException(int errorCode, String message) {
        super(errorCode, message);
    }

    public LlmServerUnavailableException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}