package org.edu.pet.task_tracker_summarization_server.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SummarizationRequest(

        @NotNull
        UUID requestId,

        @NotNull
        @Size(max = 100)
        List<TaskItem> tasks) {

}