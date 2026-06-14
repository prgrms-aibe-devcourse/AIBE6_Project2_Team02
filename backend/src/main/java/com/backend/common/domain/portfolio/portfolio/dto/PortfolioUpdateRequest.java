package com.backend.common.domain.portfolio.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 포트폴리오 수정 요청 DTO
 */
public record PortfolioUpdateRequest(
        @NotBlank(message = "포트폴리오 제목은 필수입니다.")
        String title,

        @NotBlank(message = "소개글을 입력해주세요.")
        String introduction,

        String githubUrl,
        String blogUrl,
        String deployUrl,
        String desiredPosition,
        boolean isPublished,
        List<String> techStacks // 💡 수정할 기술 스택 이름 리스트
) {
}
