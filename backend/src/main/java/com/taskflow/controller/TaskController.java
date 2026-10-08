package com.taskflow.controller;

import com.taskflow.dto.TaskDto;
import com.taskflow.dto.TaskRequest;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getAll(@PathVariable Long projectId, Authentication auth) {
        return ResponseEntity.ok(taskService.getAllForProject(projectId, currentUser(auth)));
    }

    @PostMapping
    public ResponseEntity<TaskDto> create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request,
                                           Authentication auth) {
        return ResponseEntity.ok(taskService.create(projectId, request, currentUser(auth)));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> update(@PathVariable Long projectId, @PathVariable Long taskId,
                                           @Valid @RequestBody TaskRequest request, Authentication auth) {
        return ResponseEntity.ok(taskService.update(projectId, taskId, request, currentUser(auth)));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long taskId,
                                        Authentication auth) {
        taskService.delete(projectId, taskId, currentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
