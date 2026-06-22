package com.backend.common.domain.bookmark.dto;

import com.backend.common.domain.project.project.dto.ProjectResponse;

import java.time.LocalDateTime;

public record BookmarkedProjectResponse(
        LocalDateTime bookmarkedAt,
        ProjectResponse project
) {
}
