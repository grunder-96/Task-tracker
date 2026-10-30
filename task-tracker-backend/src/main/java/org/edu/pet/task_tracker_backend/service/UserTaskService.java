package org.edu.pet.task_tracker_backend.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.task.CreateTaskRequest;
import org.edu.pet.task_tracker_backend.dto.task.TaskResponse;
import org.edu.pet.task_tracker_backend.dto.task.UpdateTaskRequest;
import org.edu.pet.task_tracker_backend.entity.UserTaskEntity;
import org.edu.pet.task_tracker_backend.exception.UserTaskNotFoundException;
import org.edu.pet.task_tracker_backend.mapper.UserTaskMapper;
import org.edu.pet.task_tracker_backend.repository.UserTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTaskService {

    private final UserTaskRepository userTaskRepository;

    private final UserTaskMapper userTaskMapper;

    @Transactional(readOnly = true)
    public List<TaskResponse> getUserTasks(Long userId) {

        return userTaskRepository.findAllByUserId(userId)
                .stream()
                .map(userTaskMapper::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getUserTaskById(Long taskId, Long userId) {

        return userTaskRepository.findByIdAndUserId(taskId, userId)
                .map(userTaskMapper::toTaskResponse)
                .orElseThrow(() -> new UserTaskNotFoundException("User task with id %d not found".formatted(taskId)));
    }

    public TaskResponse createUserTask(CreateTaskRequest request, Long userId) {

        UserTaskEntity task = userTaskMapper.toUserTask(request);
        task.setUserId(userId);

        return userTaskMapper.toTaskResponse(userTaskRepository.save(task));
    }

    public void deleteUserTaskById(Long taskId, Long userId) {

        int count = userTaskRepository.deleteByIdAndUserId(taskId, userId);

        if (count == 0) {
            throw new UserTaskNotFoundException("User task with id %d not found".formatted(taskId));
        }
    }

    public TaskResponse updateUserTask(Long taskId, Long userId, UpdateTaskRequest request) {

        UserTaskEntity userTask = userTaskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new UserTaskNotFoundException("User task with id %d not found".formatted(taskId)));

        if (request.title() != null) {
            userTask.setTitle(request.title());
        }

        if (request.description() != null) {
            userTask.setDescription(request.description());
        }

        if (request.done() != null) {
            userTask.changeDone(request.done(), Instant.now());
        }

        return userTaskMapper.toTaskResponse(userTask);
    }
}