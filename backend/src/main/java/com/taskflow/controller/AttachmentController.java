package com.taskflow.controller;

import com.taskflow.dto.AttachmentDto;
import com.taskflow.entity.Attachment;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final UserRepository userRepository;

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @GetMapping
    public ResponseEntity<List<AttachmentDto>> getAll(@PathVariable Long projectId, @PathVariable Long taskId,
                                                        Authentication auth) {
        return ResponseEntity.ok(attachmentService.getForTask(projectId, taskId, currentUser(auth)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDto> upload(@PathVariable Long projectId, @PathVariable Long taskId,
                                                 @RequestParam("file") MultipartFile file, Authentication auth) {
        return ResponseEntity.ok(attachmentService.upload(projectId, taskId, file, currentUser(auth)));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long projectId, @PathVariable Long taskId,
                                                         @PathVariable Long attachmentId, Authentication auth) {
        Attachment attachment = attachmentService.getEntityForDownload(projectId, taskId, attachmentId, currentUser(auth));
        FileSystemResource resource = new FileSystemResource(attachment.getStoragePath());

        MediaType mediaType = attachment.getContentType() != null
                ? MediaType.parseMediaType(attachment.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long taskId,
                                        @PathVariable Long attachmentId, Authentication auth) {
        attachmentService.delete(projectId, taskId, attachmentId, currentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
