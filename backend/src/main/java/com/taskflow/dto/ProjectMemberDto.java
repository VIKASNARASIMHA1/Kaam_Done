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
public class ProjectMemberDto {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private ProjectRole role;
    private LocalDateTime joinedAt;
}
