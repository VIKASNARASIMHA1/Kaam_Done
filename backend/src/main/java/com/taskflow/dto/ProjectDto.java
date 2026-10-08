package com.taskflow.dto;

import com.taskflow.entity.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private long totalTasks;
    private long doneTasks;
    private String ownerUsername;
    private ProjectRole myRole;
    private long memberCount;
}
