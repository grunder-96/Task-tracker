package org.edu.pet.task_tracker_backend.dto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailNormalizer {

    String normalize(String email){

        if (email == null) {
            return null;
        }

        return email.trim();
    }
}