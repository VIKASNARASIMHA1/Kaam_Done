package com.taskflow.controller;

import com.taskflow.dto.ActivityLogDto;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.ActivityLogService;
import com.taskflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogDto>> getRecent(@PathVariable Long projectId, Authentication auth) {
        Project project = projectService.getEntityById(projectId, currentUser(auth));
        return ResponseEntity.ok(activityLogService.getRecent(project, 50));
    }
}
