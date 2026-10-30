package org.edu.pet.task_tracker_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank
    @Email
    @Size(max = 254, message = "must not exceed 254 characters")
    String email,

    @NotBlank
    @Size(min = 8, max = 128, message = "must be between {min} and {max} characters")
    String password
) {

    public RegisterRequest {
        email = StringNormalizer.strip(email);
    }
}