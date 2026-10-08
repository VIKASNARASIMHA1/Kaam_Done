package com.taskflow.service;

import com.taskflow.dto.AddMemberRequest;
import com.taskflow.dto.ProjectDto;
import com.taskflow.dto.ProjectMemberDto;
import com.taskflow.dto.ProjectRequest;
import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectMember;
import com.taskflow.entity.ProjectRole;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.taskflow.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService accessService;
    private final ActivityLogService activityLogService;

    public List<ProjectDto> getAllForUser(User user) {
        return projectRepository.findAccessibleByUser(user).stream()
                .map(project -> toDto(project, user))
                .toList();
    }

    public ProjectDto getById(Long id, User user) {
        Project project = accessService.requireProject(id, user);
        return toDto(project, user);
    }

    public ProjectDto create(ProjectRequest request, User user) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(user)
                .build();
        Project saved = projectRepository.save(project);
        activityLogService.log(saved, user, "PROJECT_CREATED", user.getUsername() + " created the project");
        return toDto(saved, user);
    }

    public ProjectDto update(Long id, ProjectRequest request, User user) {
        Project project = accessService.requireProject(id, user);
        accessService.requireEditorOrAbove(project, user);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        Project saved = projectRepository.save(project);
        activityLogService.log(saved, user, "PROJECT_UPDATED", user.getUsername() + " updated the project details");
        return toDto(saved, user);
    }

    public void delete(Long id, User user) {
        Project project = accessService.requireProject(id, user);
        accessService.requireOwner(project, user);
        projectRepository.delete(project);
    }

    public Project getEntityById(Long id, User user) {
        return accessService.requireProject(id, user);
    }

    // ---- Member management ----

    public List<ProjectMemberDto> getMembers(Long projectId, User user) {
        Project project = accessService.requireProject(projectId, user);
        List<ProjectMemberDto> members = projectMemberRepository.findByProject(project).stream()
                .map(this::toMemberDto)
                .collect(java.util.stream.Collectors.toList());

        // include the owner as a pseudo-member so the frontend can render one unified list
        members.add(0, ProjectMemberDto.builder()
                .id(-1L)
                .userId(project.getOwner().getId())
                .username(project.getOwner().getUsername())
                .email(project.getOwner().getEmail())
                .role(ProjectRole.OWNER)
                .joinedAt(project.getCreatedAt())
                .build());

        return members;
    }

    public ProjectMemberDto addMember(Long projectId, AddMemberRequest request, User user) {
        Project project = accessService.requireProject(projectId, user);
        accessService.requireOwner(project, user);

        User target = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with username: " + request.getUsername()));

        if (target.getId().equals(project.getOwner().getId())) {
            throw new IllegalArgumentException("This user already owns the project");
        }
        if (projectMemberRepository.existsByProjectAndUser(project, target)) {
            throw new IllegalArgumentException("This user is already a member of the project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(target)
                .role(request.getRole() != null ? request.getRole() : ProjectRole.EDITOR)
                .build();
        ProjectMember saved = projectMemberRepository.save(member);

        activityLogService.log(project, user,
                "MEMBER_ADDED",
                user.getUsername() + " added " + target.getUsername() + " as " + saved.getRole());

        return toMemberDto(saved);
    }

    public ProjectMemberDto updateMemberRole(Long projectId, Long memberId, ProjectRole newRole, User user) {
        Project project = accessService.requireProject(projectId, user);
        accessService.requireOwner(project, user);

        ProjectMember member = projectMemberRepository.findById(memberId)
                .filter(m -> m.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        member.setRole(newRole);
        ProjectMember saved = projectMemberRepository.save(member);

        activityLogService.log(project, user,
                "MEMBER_ROLE_CHANGED",
                user.getUsername() + " changed " + member.getUser().getUsername() + "'s role to " + newRole);

        return toMemberDto(saved);
    }

    public void removeMember(Long projectId, Long memberId, User user) {
        Project project = accessService.requireProject(projectId, user);
        accessService.requireOwner(project, user);

        ProjectMember member = projectMemberRepository.findById(memberId)
                .filter(m -> m.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        projectMemberRepository.delete(member);
        activityLogService.log(project, user,
                "MEMBER_REMOVED",
                user.getUsername() + " removed " + member.getUser().getUsername() + " from the project");
    }

    // ---- mapping helpers ----

    private ProjectMemberDto toMemberDto(ProjectMember member) {
        return ProjectMemberDto.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private ProjectDto toDto(Project project, User currentUser) {
        List<Task> tasks = taskRepository.findByProject(project);
        long total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long memberCount = projectMemberRepository.findByProject(project).size() + 1; // +1 for owner

        ProjectRole myRole;
        try {
            myRole = accessService.getRole(project, currentUser);
        } catch (ForbiddenException ex) {
            myRole = null;
        }

        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .totalTasks(total)
                .doneTasks(done)
                .ownerUsername(project.getOwner().getUsername())
                .myRole(myRole)
                .memberCount(memberCount)
                .build();
    }
}
