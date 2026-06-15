package com.backend.common.domain.portfolio.portfolio.controller;

import com.backend.common.domain.portfolio.portfolio.dto.PortfolioResponse;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioUpdateRequest;
import com.backend.common.domain.portfolio.portfolio.service.PortfolioService;
import com.backend.common.domain.portfolio.proposals.dto.MyPageProposalResponse;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 내 포트폴리오 조회
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public RsData<PortfolioResponse> getMyPortfolio(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        PortfolioResponse response = portfolioService.getMyPortfolio(userDetails.getMemberId());
        return RsData.of("200", "내 포트폴리오 조회가 완료되었습니다.", response);
    }

    /**
     * 내 포트폴리오 수정
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public RsData<PortfolioResponse> updatePortfolio(
            @AuthenticationPrincipal CustomMemberDetails userDetails,
            @Valid @RequestBody PortfolioUpdateRequest request
    ) {
        PortfolioResponse response = portfolioService.updatePortfolio(userDetails.getMemberId(), request);
        return RsData.of("200", "포트폴리오 수정이 완료되었습니다.", response);
    }

    /**
     * 내 포폴에 온 제안 목록 조회
     */
    @GetMapping("/me/proposals") // 내 포폴(/me) 하위의 자원(/proposals) 명시
    @PreAuthorize("isAuthenticated()")
    public RsData<List<MyPageProposalResponse>> getMyReceivedProposals(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<MyPageProposalResponse> responses = portfolioService.getMyReceivedProposals(userDetails.getMemberId());
        return RsData.of("200", "받은 프로젝트 제안 목록 조회가 완료되었습니다.", responses);
    }

}
