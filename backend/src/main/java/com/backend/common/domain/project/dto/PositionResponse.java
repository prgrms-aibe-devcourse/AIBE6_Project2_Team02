package com.backend.common.domain.project.dto;

public record PositionResponse(
        String role,
        int filled,
        int total
) {
}
