package com.backend.common.domain.project.dto;

import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.project.entity.ProjectRole;

import java.util.List;

public record PMResponse(
        String id,
        String name,
        String avatar,
        ProjectRole role,
        String bio,
        List<String> techStack,
        String location,
        boolean featured,
        PositionType position

) {
}
