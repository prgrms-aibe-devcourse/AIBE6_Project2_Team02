package com.backend.common.domain.project.project.dto;

import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.PositionType;

import java.util.List;

public record ProjectUpdateRequest(
        String title,
        String description,
        ProjectCategory category,
        List<String> goals,
        List<String> techStacks,
        String deadline,
        boolean open,
        PositionType leaderPosition,
        List<PositionUpdateRequest> positions
) {
}
