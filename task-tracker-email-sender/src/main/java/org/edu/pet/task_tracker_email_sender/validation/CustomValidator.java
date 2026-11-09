package org.edu.pet.task_tracker_email_sender.validation;

import jakarta.validation.Validation;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

public class CustomValidator extends SpringValidatorAdapter {

    public CustomValidator() {
        super(Validation.buildDefaultValidatorFactory().getValidator());
    }
}