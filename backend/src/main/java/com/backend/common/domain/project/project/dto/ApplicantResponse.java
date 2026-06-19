package com.backend.common.domain.project.project.dto;

import java.util.List;

public record ApplicantResponse(
        String id,
        String nickname,
        String profileImageUrl,
        String position,
        String message,
        List<String> techStacks
) {}
