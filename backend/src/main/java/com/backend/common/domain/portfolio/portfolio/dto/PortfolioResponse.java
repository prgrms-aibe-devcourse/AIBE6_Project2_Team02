package com.backend.common.domain.portfolio.portfolio.dto;

import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.entity.PortfolioLink;

import java.util.List;

public record PortfolioResponse(
        Long id,
        String title,
        String introduction,
        List<PortfolioLink> links,
        String desiredPosition,
        boolean isPublished,
        List<String> techStacks,
        String userNickname,
        String userProfileImageUrl
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        List<String> stacks = portfolio.getPortfolioTechStacks().stream()
                .map(pts -> pts.getTechStack().getName())
                .toList();

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getIntroduction(),
                portfolio.getLinks(),
                portfolio.getDesiredPosition(),
                portfolio.isPublished(),
                stacks,
                portfolio.getMember().getNickname(),
                portfolio.getMember().getProfileImageUrl()
        );
    }
}
