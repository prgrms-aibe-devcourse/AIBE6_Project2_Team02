package com.backend.common.domain.bookmark.dto;

import com.backend.common.domain.portfolio.portfolio.dto.PortfolioListResponse;

import java.time.LocalDateTime;

public record BookmarkedPortfolioResponse(
        LocalDateTime bookmarkedAt,
        PortfolioListResponse portfolio
) {
}
