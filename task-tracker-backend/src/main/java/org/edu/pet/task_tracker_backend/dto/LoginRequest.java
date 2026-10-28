package org.edu.pet.task_tracker_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 128, message = "must not exceed {max} characters")
        String password) {

        public LoginRequest {
            email = EmailNormalizer.normalize(email);
        }
}