package com.backend.common.domain.project.project.dto;

import com.backend.common.domain.project.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusUpdateRequest(
        @NotNull(message = "변경할 상태 값은 필수입니다.")
        ProjectStatus status
) {}
