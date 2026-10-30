package org.edu.pet.task_tracker_backend.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.edu.pet.task_tracker_backend.dto.StringNormalizer;

public record CreateTaskRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 5_000)
        String description) {

    public CreateTaskRequest {

        title = StringNormalizer.strip(title);
        description = description == null ? "" : description;
    }
}