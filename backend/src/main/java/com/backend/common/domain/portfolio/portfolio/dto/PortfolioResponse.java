package com.backend.common.domain.portfolio.portfolio.dto;

import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;

import java.util.List;

/**
 * 포트폴리오 상세 조회 응답 DTO
 */
public record PortfolioResponse(
        Long id,
        String title,
        String introduction,
        String githubUrl,
        String blogUrl,
        String deployUrl,
        String desiredPosition,
        boolean isPublished,
        List<String> techStacks // 💡 PortfolioTechStack에서 추출할 스택명 리스트
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        List<String> stacks = portfolio.getPortfolioTechStacks().stream()
                .map(pts -> pts.getTechStack().getName())
                .toList();

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getIntroduction(),
                portfolio.getGithubUrl(),
                portfolio.getBlogUrl(),
                portfolio.getDeployUrl(),
                portfolio.getDesiredPosition(),
                portfolio.isPublished(),
                stacks
        );
    }
}
