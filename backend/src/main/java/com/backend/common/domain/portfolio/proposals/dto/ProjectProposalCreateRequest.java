package com.backend.common.domain.portfolio.proposals.dto;

public record ProjectProposalCreateRequest(
        Long projectId,
        String position,
        String message
) {
}
