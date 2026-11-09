package org.edu.pet.task_tracker_email_sender.event;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailSendingEvent(

        @NotBlank
        @Email
        String to,

        @NotBlank
        @Pattern(regexp = "^[^\\r\\n]*$")
        String subject,

        @NotBlank
        String body) {

}