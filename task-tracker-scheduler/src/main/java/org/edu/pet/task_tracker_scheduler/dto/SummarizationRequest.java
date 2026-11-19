package org.edu.pet.task_tracker_scheduler.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.edu.pet.task_tracker_scheduler.model.UserTask;

import java.util.List;
import java.util.UUID;

public record SummarizationRequest(

        @NotNull
        UUID requestId,

        @NotNull
        @Size(max = 100)
        List<UserTask> tasks) {

}