package com.backend.common.domain.project.dto;

import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.PositionType;

import java.util.List;

public record ProjectCreateRequest(
        String title,
        String description,
        String fullDescription,
        ProjectCategory category,
        List<String> goals,
        String deadline,
        boolean open,
        PositionType leaderPosition,
        Long leaderId,
        List<Long> memberIds,
        List<String> techStacks,
        List<PositionCreateRequest> positions
) {

}
