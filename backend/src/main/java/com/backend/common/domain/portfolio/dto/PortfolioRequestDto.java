package com.backend.common.domain.portfolio.dto;

import java.util.List;

public record PortfolioRequestDto (
        String title,
        String introduction,
        String githubUrl,
        String blogUrl,
        String deployUrl,
        String desiredPosition,
        List<Long> techStackIds,
        boolean isPublished
) {
}
