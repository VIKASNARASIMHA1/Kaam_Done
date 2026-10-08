package com.taskflow.repository;

import com.taskflow.entity.Comment;
import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskOrderByCreatedAtAsc(Task task);
    long countByTask(Task task);
}
