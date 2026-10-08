package com.taskflow.repository;

import com.taskflow.entity.ActivityLog;
import com.taskflow.entity.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByProjectOrderByCreatedAtDesc(Project project, Pageable pageable);
}
