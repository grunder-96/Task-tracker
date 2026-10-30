package org.edu.pet.task_tracker_backend.dto.task;

public record TaskResponse(Long id, String title, String description, boolean done) {

}