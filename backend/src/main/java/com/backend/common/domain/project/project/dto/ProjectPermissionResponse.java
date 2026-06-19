package com.backend.common.domain.project.project.dto;

public record ProjectPermissionResponse(
        boolean canEdit,
        boolean isMember,
        boolean isLeader,
        Long pendingApplicationId
) {
}
