package com.taskflow.repository;

import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    Optional<Task> findByIdAndProject(Long id, Project project);
    long countByProject(Project project);
}
