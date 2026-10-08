package com.taskflow.dto;

import com.taskflow.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberRoleRequest {
    @NotNull
    private ProjectRole role;
}
