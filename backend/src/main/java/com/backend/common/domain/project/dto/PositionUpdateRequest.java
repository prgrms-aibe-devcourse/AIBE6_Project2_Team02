package com.backend.common.domain.project.dto;

public record PositionUpdateRequest(
        String role,
        int total
) {
}
