package com.backend.api.dto;

import java.util.List;

public record ProjectResponse(
        String id,
        String title,
        String description,
        String fullDescription,
        List<String> goals,
        List<String> techStack,
        List<PositionResponse> positions,
        String recruitmentStatus,
        String category,
        UserResponse leader,
        List<UserResponse> teamMembers,
        String deadline,
        String createdAt,
        int popularity,
        boolean featured
) {
}
