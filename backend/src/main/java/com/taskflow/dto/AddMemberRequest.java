package com.taskflow.dto;

import com.taskflow.entity.ProjectRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotBlank
    private String username;
    private ProjectRole role;
}
