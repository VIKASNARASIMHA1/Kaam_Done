package com.taskflow.repository;

import com.taskflow.entity.Attachment;
import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTask(Task task);
    Optional<Attachment> findByIdAndTask(Long id, Task task);
    long countByTask(Task task);
}
