package com.backend.common.domain.portfolio.portfolio.controller;

import com.backend.common.domain.portfolio.portfolio.dto.PortfolioResponse;
import com.backend.common.domain.portfolio.portfolio.service.AdminPortfolioService;
import com.backend.common.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/portfolios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPortfolioController {

    private final AdminPortfolioService adminPortfolioService;

    @GetMapping("/hidden")
    public ResponseEntity<RsData<List<PortfolioResponse>>> getHiddenPortfolios() {
        List<PortfolioResponse> portfolios = adminPortfolioService.getHiddenPortfolios();
        return ResponseEntity.ok(
                RsData.of("200", "숨겨진 포트폴리오 목록 조회 성공", portfolios)
        );
    }

    @PatchMapping("/{portfolioId}/unhide")
    public ResponseEntity<RsData<Void>> unhidePortfolio(@PathVariable Long portfolioId) {
        adminPortfolioService.unhidePortfolio(portfolioId);
        return ResponseEntity.ok(
                RsData.of("200", "포트폴리오 숨김이 해제되었습니다.")
        );
    }
}
