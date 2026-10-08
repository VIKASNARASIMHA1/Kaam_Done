package com.taskflow.service;

import com.taskflow.dto.TaskDto;
import com.taskflow.dto.TaskRequest;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.AttachmentRepository;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectAccessService accessService;
    private final ActivityLogService activityLogService;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    public List<TaskDto> getAllForProject(Long projectId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        return taskRepository.findByProject(project).stream()
                .map(this::toDto)
                .toList();
    }

    public TaskDto getOne(Long projectId, Long taskId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return toDto(task);
    }

    public TaskDto create(Long projectId, TaskRequest request, User user) {
        Project project = projectService.getEntityById(projectId, user);
        accessService.requireEditorOrAbove(project, user);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .project(project)
                .build();
        Task saved = taskRepository.save(task);

        activityLogService.log(project, user, "TASK_CREATED",
                user.getUsername() + " created task \"" + saved.getTitle() + "\"");

        return toDto(saved);
    }

    public TaskDto update(Long projectId, Long taskId, TaskRequest request, User user) {
        Project project = projectService.getEntityById(projectId, user);
        accessService.requireEditorOrAbove(project, user);

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        boolean statusChanged = request.getStatus() != null && request.getStatus() != task.getStatus();
        TaskStatus previousStatus = task.getStatus();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        Task saved = taskRepository.save(task);

        if (statusChanged) {
            activityLogService.log(project, user, "TASK_STATUS_CHANGED",
                    user.getUsername() + " moved \"" + saved.getTitle() + "\" from " + previousStatus + " to " + saved.getStatus());
        } else {
            activityLogService.log(project, user, "TASK_UPDATED",
                    user.getUsername() + " updated task \"" + saved.getTitle() + "\"");
        }

        return toDto(saved);
    }

    public void delete(Long projectId, Long taskId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        accessService.requireEditorOrAbove(project, user);

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        String title = task.getTitle();
        taskRepository.delete(task);
        activityLogService.log(project, user, "TASK_DELETED",
                user.getUsername() + " deleted task \"" + title + "\"");
    }

    Task getEntity(Long projectId, Long taskId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        return taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private TaskDto toDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .projectId(task.getProject().getId())
                .commentCount(commentRepository.countByTask(task))
                .attachmentCount(attachmentRepository.countByTask(task))
                .build();
    }
}
