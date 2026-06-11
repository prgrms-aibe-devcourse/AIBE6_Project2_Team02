package com.backend.api.dto;

public record PositionResponse(
        String role,
        int filled,
        int total
) {
}
