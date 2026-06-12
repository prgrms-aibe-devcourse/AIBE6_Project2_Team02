package com.backend.common.domain.portfolio.controller;

import com.backend.common.domain.portfolio.dto.PortfolioRequestDto;
import com.backend.common.domain.portfolio.service.PortfolioService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

   @PostMapping
    public RsData<Void> createPortfolio(
           @AuthenticationPrincipal CustomMemberDetails userDetails,
           @RequestBody PortfolioRequestDto dto
    ){
        portfolioService.createPortfolio(userDetails.getMemberId(), dto);
        return RsData.of("200", "개인 포트폴리오가 등록 되었습니다");
   }
}
