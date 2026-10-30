package org.edu.pet.task_tracker_backend.dto.task;

import jakarta.validation.constraints.Size;
import org.edu.pet.task_tracker_backend.dto.StringNormalizer;

public record UpdateTaskRequest(

        @Size(min = 1, max = 255)
        String title,

        @Size(max = 5_000)
        String description,

        Boolean done) {

        public UpdateTaskRequest {
                title = StringNormalizer.strip(title);
        }
}