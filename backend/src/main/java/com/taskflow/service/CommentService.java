package com.taskflow.service;

import com.taskflow.dto.CommentDto;
import com.taskflow.dto.CommentRequest;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.taskflow.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectAccessService accessService;
    private final ActivityLogService activityLogService;

    private Task resolveTask(Long projectId, Long taskId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        return taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    public List<CommentDto> getForTask(Long projectId, Long taskId, User user) {
        Task task = resolveTask(projectId, taskId, user);
        return commentRepository.findByTaskOrderByCreatedAtAsc(task).stream()
                .map(this::toDto)
                .toList();
    }

    public CommentDto addComment(Long projectId, Long taskId, CommentRequest request, User user) {
        Project project = projectService.getEntityById(projectId, user);
        accessService.requireEditorOrAbove(project, user);

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(user)
                .task(task)
                .build();
        Comment saved = commentRepository.save(comment);

        activityLogService.log(project, user, "COMMENT_ADDED",
                user.getUsername() + " commented on \"" + task.getTitle() + "\"");

        return toDto(saved);
    }

    public void deleteComment(Long projectId, Long taskId, Long commentId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getTask().getId().equals(task.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        if (!isAuthor && !isOwner) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(comment.getAuthor().getUsername())
                .authorId(comment.getAuthor().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
