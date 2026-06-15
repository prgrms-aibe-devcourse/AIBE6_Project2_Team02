package com.backend.common.domain.project.dto;

import com.backend.common.domain.project.enums.ProjectCategory;
import java.util.List;

public record ProjectCreateRequest(
        String title,
        String description,
        String fullDescription,
        ProjectCategory category,
        List<String> goals,
        String deadline,
        boolean open,
        List<String> techStacks,
        List<PositionCreateRequest> positions
) {

}
