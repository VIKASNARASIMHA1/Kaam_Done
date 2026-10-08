package com.taskflow.service;

import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectMember;
import com.taskflow.entity.ProjectRole;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.taskflow.exception.ForbiddenException;
import org.springframework.stereotype.Service;

/**
 * Centralizes membership + role checks so every service (tasks, comments,
 * attachments, activity log) enforces the same rules:
 *  - OWNER: full control, including managing members and deleting the project
 *  - EDITOR: can create/update/delete tasks, comments, attachments
 *  - VIEWER: read-only access
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public Project requireProject(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        getRole(project, user);
        return project;
    }

    public ProjectRole getRole(Project project, User user) {
        if (project.getOwner().getId().equals(user.getId())) {
            return ProjectRole.OWNER;
        }
        return projectMemberRepository.findByProjectAndUser(project, user)
                .map(ProjectMember::getRole)
                .orElseThrow(() -> new ForbiddenException("You do not have access to this project"));
    }

    public void requireEditorOrAbove(Project project, User user) {
        if (getRole(project, user) == ProjectRole.VIEWER) {
            throw new ForbiddenException("You only have view access to this project");
        }
    }

    public void requireOwner(Project project, User user) {
        if (getRole(project, user) != ProjectRole.OWNER) {
            throw new ForbiddenException("Only the project owner can do this");
        }
    }
}
