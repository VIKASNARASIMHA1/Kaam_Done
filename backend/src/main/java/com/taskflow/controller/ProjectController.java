package com.taskflow.controller;

import com.taskflow.dto.AddMemberRequest;
import com.taskflow.dto.ProjectDto;
import com.taskflow.dto.ProjectMemberDto;
import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.UpdateMemberRoleRequest;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAll(Authentication auth) {
        return ResponseEntity.ok(projectService.getAllForUser(currentUser(auth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getById(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(projectService.getById(id, currentUser(auth)));
    }

    @PostMapping
    public ResponseEntity<ProjectDto> create(@Valid @RequestBody ProjectRequest request, Authentication auth) {
        return ResponseEntity.ok(projectService.create(request, currentUser(auth)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request,
                                              Authentication auth) {
        return ResponseEntity.ok(projectService.update(id, request, currentUser(auth)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        projectService.delete(id, currentUser(auth));
        return ResponseEntity.noContent().build();
    }

    // ---- Members ----

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ProjectMemberDto>> getMembers(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(projectService.getMembers(id, currentUser(auth)));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectMemberDto> addMember(@PathVariable Long id,
                                                       @Valid @RequestBody AddMemberRequest request,
                                                       Authentication auth) {
        return ResponseEntity.ok(projectService.addMember(id, request, currentUser(auth)));
    }

    @PutMapping("/{id}/members/{memberId}")
    public ResponseEntity<ProjectMemberDto> updateMemberRole(@PathVariable Long id, @PathVariable Long memberId,
                                                              @Valid @RequestBody UpdateMemberRoleRequest request,
                                                              Authentication auth) {
        return ResponseEntity.ok(projectService.updateMemberRole(id, memberId, request.getRole(), currentUser(auth)));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId, Authentication auth) {
        projectService.removeMember(id, memberId, currentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
