package com.backend.common.domain.project.dto;

public record ProjectPermissionResponse(
        boolean canEdit,
        boolean isMember
) {
}
