package com.backend.common.domain.project.dto;

import com.backend.common.domain.project.enums.ProjectCategory;

import java.util.List;

public record ProjectUpdateRequest(
        String title,
        String description,
        String fullDescription,
        ProjectCategory category,
        List<String> goals,
        List<String> techStacks,
        String deadline,
        boolean open,
        List<PositionUpdateRequest> positions
) {
}
