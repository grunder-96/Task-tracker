package org.edu.pet.task_tracker_summarization_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskItem(

        @NotBlank
        @Size(max = 255)
        String title,

        boolean done) {

}