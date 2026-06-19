package com.backend.common.domain.project.project.dto;

import com.backend.common.domain.project.project.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record ProjectRoleUpdateRequest(
        @NotNull(message = "변경할 권한 등급은 필수입니다.")
        ProjectRole role // LEADER, MANAGER, MEMBER 중 하나를 이넘으로 수신
) {}
