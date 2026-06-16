package com.backend.common.domain.project.dto;

public record ProjectApplicationCreateRequest(
        String position,
        String message
) {
}
