package com.backend.common.domain.portfolio.portfolio.repository;

import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByMemberId(Long memberId);
}
