package com.backend.common.domain.portfolio.portfolio.service;

import com.backend.common.domain.portfolio.portfolio.dto.PortfolioResponse;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPortfolioService {

    private final PortfolioRepository portfolioRepository;

    public List<PortfolioResponse> getHiddenPortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findByIsHiddenTrueOrderByUpdatedAtDesc();
        return portfolios.stream()
                .map(PortfolioResponse::from)
                .toList();
    }

    @Transactional
    public void unhidePortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NoSuchElementException("Portfolio not found"));
        portfolio.unhide();
    }
}
