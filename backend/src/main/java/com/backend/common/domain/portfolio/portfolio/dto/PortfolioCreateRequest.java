package com.backend.common.domain.portfolio.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PortfolioCreateRequest(
        @NotBlank(message = "포트폴리오 제목은 필수입니다.")
        String title,

        String introduction,

        @NotBlank(message = "희망 포지션은 필수입니다.")
        String desiredPosition,

        String githubUrl,
        String blogUrl,
        String deployUrl,

        boolean isPublished,
        List<Long> techStackIds
) {
}
