package com.backend.api.dto;

public record PositionCreateRequest(
        String role,
        int total
) {
}

