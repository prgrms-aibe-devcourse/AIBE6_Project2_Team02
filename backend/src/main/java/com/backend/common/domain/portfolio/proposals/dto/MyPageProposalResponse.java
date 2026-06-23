package com.backend.common.domain.portfolio.proposals.dto;

import com.backend.common.domain.portfolio.proposals.entity.ProjectProposal;

import java.time.LocalDateTime;

public record MyPageProposalResponse(
        Long proposalId,
        Long projectId,
        String projectTitle,
        String position,
        String proposerName,
        String message,
        String status,
        LocalDateTime createdAt
) {
    public static MyPageProposalResponse from(ProjectProposal proposal) {
        return new MyPageProposalResponse(
                proposal.getId(),
                proposal.getProject().getId(),
                proposal.getProject().getTitle(),
                proposal.getPosition() != null ? proposal.getPosition().name() : null,
                proposal.getProposer().getNickname(),
                proposal.getMessage(),
                proposal.getStatus().name(),
                proposal.getCreatedAt()
        );
    }
}
