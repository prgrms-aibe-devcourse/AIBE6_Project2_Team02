package com.backend.common.domain.project.dto;

public record PositionCreateRequest(
        String role,
        int total
) {
}

