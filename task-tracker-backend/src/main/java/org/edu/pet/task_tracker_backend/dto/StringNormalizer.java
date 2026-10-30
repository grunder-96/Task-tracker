package org.edu.pet.task_tracker_backend.dto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringNormalizer {

    public String strip(String value){

        if (value == null) {
            return null;
        }

        return value.strip();
    }
}