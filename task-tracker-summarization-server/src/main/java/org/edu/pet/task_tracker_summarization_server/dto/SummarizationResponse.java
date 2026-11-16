package org.edu.pet.task_tracker_summarization_server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SummarizationResponse {

    private final boolean success;
    private final String report;
    private final String errorMessage;

    @JsonCreator
    private SummarizationResponse(boolean success, String report, String errorMessage) {

        this.success = success;
        this.report = report;
        this.errorMessage = errorMessage;
    }

    public static SummarizationResponse success(String text) {
        return new SummarizationResponse(true, text, null);
    }

    public static SummarizationResponse failed(String errorMessage) {
        return new SummarizationResponse(false, null, errorMessage);
    }
}