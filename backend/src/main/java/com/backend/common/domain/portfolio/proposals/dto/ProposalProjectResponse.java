package com.backend.common.domain.portfolio.proposals.dto;

import com.backend.common.domain.project.project.entity.Project;

import java.util.List;

public record ProposalProjectResponse(
        Long id,
        String title,
        List<ProposalPositionResponse> positions
) {
    public static ProposalProjectResponse from(Project project, List<ProposalPositionResponse> positions) {
        return new ProposalProjectResponse(project.getId(), project.getTitle(), positions);
    }

    public record ProposalPositionResponse(
            String role,
            long filled,
            int total
    ) {
    }
}
