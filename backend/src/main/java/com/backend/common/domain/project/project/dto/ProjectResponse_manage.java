package com.backend.common.domain.project.project.dto;

import com.backend.common.domain.member.dto.UserResponse;
import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.ProjectStatus;

import java.util.List;

public record ProjectResponse_manage(
        String id,
        String title,
        String description,
        List<String> goals,
        List<String> techStack,
        List<PositionResponse> positions,
        ProjectStatus recruitmentStatus,
        ProjectCategory category,
        UserResponse leader,
        List<UserResponse> teamMembers,
        String deadline,
        String createdAt,
        int popularity,
        boolean featured,
        List<PMResponse> pmResponses
) {
}
