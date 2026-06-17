package com.backend.common.domain.project.dto;

import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.RecruitmentStatus;

import java.util.List;

public record ProjectResponse_manage(
        String id,
        String title,
        String description,
        String fullDescription,
        List<String> goals,
        List<String> techStack,
        List<PositionResponse> positions,
        RecruitmentStatus recruitmentStatus,
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
