package com.taskflow.service;

import com.taskflow.dto.ActivityLogDto;
import com.taskflow.entity.ActivityLog;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void log(Project project, User actor, String action, String description) {
        ActivityLog entry = ActivityLog.builder()
                .project(project)
                .actor(actor)
                .action(action)
                .description(description)
                .build();
        activityLogRepository.save(entry);
    }

    public List<ActivityLogDto> getRecent(Project project, int limit) {
        return activityLogRepository.findByProjectOrderByCreatedAtDesc(project, PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ActivityLogDto toDto(ActivityLog log) {
        return ActivityLogDto.builder()
                .id(log.getId())
                .action(log.getAction())
                .description(log.getDescription())
                .actorUsername(log.getActor().getUsername())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
