package org.edu.pet.task_tracker_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password) {

        public LoginRequest {
            email = EmailNormalizer.normalize(email);
        }
}