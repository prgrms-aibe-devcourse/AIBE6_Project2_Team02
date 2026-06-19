package com.backend.common.domain.portfolio.portfolio.repository;

import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PortfolioRepositoryCustom {

    Page<Portfolio> searchPortfolios(
            String search,
            String role,
            String tech,
            Pageable pageable
    );
}
