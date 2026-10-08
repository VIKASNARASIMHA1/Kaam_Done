package com.taskflow.controller;

import com.taskflow.dto.CommentDto;
import com.taskflow.dto.CommentRequest;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getAll(@PathVariable Long projectId, @PathVariable Long taskId,
                                                     Authentication auth) {
        return ResponseEntity.ok(commentService.getForTask(projectId, taskId, currentUser(auth)));
    }

    @PostMapping
    public ResponseEntity<CommentDto> create(@PathVariable Long projectId, @PathVariable Long taskId,
                                              @Valid @RequestBody CommentRequest request, Authentication auth) {
        return ResponseEntity.ok(commentService.addComment(projectId, taskId, request, currentUser(auth)));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long taskId,
                                        @PathVariable Long commentId, Authentication auth) {
        commentService.deleteComment(projectId, taskId, commentId, currentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
