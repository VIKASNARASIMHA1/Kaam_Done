package com.taskflow.service;

import com.taskflow.dto.AttachmentDto;
import com.taskflow.entity.Attachment;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.AttachmentRepository;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectAccessService accessService;
    private final ActivityLogService activityLogService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Task resolveTask(Long projectId, Long taskId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        return taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    public List<AttachmentDto> getForTask(Long projectId, Long taskId, User user) {
        Task task = resolveTask(projectId, taskId, user);
        return attachmentRepository.findByTask(task).stream()
                .map(this::toDto)
                .toList();
    }

    public AttachmentDto upload(Long projectId, Long taskId, MultipartFile file, User user) {
        Project project = projectService.getEntityById(projectId, user);
        accessService.requireEditorOrAbove(project, user);

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was provided");
        }

        try {
            Path dir = Paths.get(uploadDir, "task-" + task.getId());
            Files.createDirectories(dir);

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String storedName = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(storedName);
            file.transferTo(target);

            Attachment attachment = Attachment.builder()
                    .originalFilename(originalName)
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .storagePath(target.toString())
                    .uploadedBy(user)
                    .task(task)
                    .build();
            Attachment saved = attachmentRepository.save(attachment);

            activityLogService.log(project, user, "ATTACHMENT_ADDED",
                    user.getUsername() + " attached \"" + originalName + "\" to \"" + task.getTitle() + "\"");

            return toDto(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public Attachment getEntityForDownload(Long projectId, Long taskId, Long attachmentId, User user) {
        Task task = resolveTask(projectId, taskId, user);
        return attachmentRepository.findByIdAndTask(attachmentId, task)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    }

    public void delete(Long projectId, Long taskId, Long attachmentId, User user) {
        Project project = projectService.getEntityById(projectId, user);
        accessService.requireEditorOrAbove(project, user);

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Attachment attachment = attachmentRepository.findByIdAndTask(attachmentId, task)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        try {
            Files.deleteIfExists(Paths.get(attachment.getStoragePath()));
        } catch (IOException ignored) {
            // if the file is already gone, still remove the DB record
        }
        attachmentRepository.delete(attachment);
    }

    private AttachmentDto toDto(Attachment attachment) {
        return AttachmentDto.builder()
                .id(attachment.getId())
                .filename(attachment.getOriginalFilename())
                .contentType(attachment.getContentType())
                .sizeBytes(attachment.getSizeBytes())
                .uploadedByUsername(attachment.getUploadedBy().getUsername())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}
