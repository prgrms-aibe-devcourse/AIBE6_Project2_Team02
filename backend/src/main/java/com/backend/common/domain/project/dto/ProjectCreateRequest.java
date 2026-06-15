package com.backend.common.domain.project.dto;

import java.util.List;

public record ProjectCreateRequest(
        String title,
        String description,
        String fullDescription,
        String category,
        List<String> goals,
        String deadline,
        boolean open,
        List<String> techStacks,
        List<PositionCreateRequest> positions
) {

}
