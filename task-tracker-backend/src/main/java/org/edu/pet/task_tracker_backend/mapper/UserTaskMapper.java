package org.edu.pet.task_tracker_backend.mapper;

import org.edu.pet.task_tracker_backend.dto.task.CreateTaskRequest;
import org.edu.pet.task_tracker_backend.dto.task.TaskResponse;
import org.edu.pet.task_tracker_backend.entity.UserTaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserTaskMapper {

    TaskResponse toTaskResponse(UserTaskEntity userTaskEntity);

    UserTaskEntity toUserTask(CreateTaskRequest request);
}