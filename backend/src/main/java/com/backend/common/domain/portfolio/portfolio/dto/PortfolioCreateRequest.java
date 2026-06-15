package com.backend.common.domain.portfolio.portfolio.dto;

import java.util.List;

public record PortfolioCreateRequest(
        String title,
        String introduction,
        String desiredPosition,
        String githubUrl,
        String blogUrl,
        String deployUrl,
        boolean isPublished,
        List<Long> techStackIds
) {
}
