package org.edu.pet.task_tracker_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.task.CreateTaskRequest;
import org.edu.pet.task_tracker_backend.dto.task.TaskResponse;
import org.edu.pet.task_tracker_backend.dto.task.UpdateTaskRequest;
import org.edu.pet.task_tracker_backend.security.CustomUserDetails;
import org.edu.pet.task_tracker_backend.service.UserTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class UserTaskController {

    private final UserTaskService userTaskService;

    @GetMapping
    public List<TaskResponse> getUserTasks(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return userTaskService.getUserTasks(customUserDetails.getId());
    }

    @GetMapping("/{taskId:\\d+}")
    public TaskResponse getUserTask(@PathVariable Long taskId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return userTaskService.getUserTaskById(taskId, customUserDetails.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createUserTask(@RequestBody @Valid CreateTaskRequest request, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return userTaskService.createUserTask(request, customUserDetails.getId());
    }

    @DeleteMapping("/{taskId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserTask(@PathVariable Long taskId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        userTaskService.deleteUserTaskById(taskId, customUserDetails.getId());
    }

    @PatchMapping("/{taskId:\\d+}")
    public TaskResponse updateUserTask(@PathVariable Long taskId,
                                       @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                       @RequestBody @Valid UpdateTaskRequest request) {

        return userTaskService.updateUserTask(taskId, customUserDetails.getId(), request);
    }
}