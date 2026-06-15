package com.backend.common.domain.portfolio.portfolio.repository;

import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByMemberId(Long memberId);

    @Query("""
            SELECT p
            FROM Portfolio p
            WHERE p.isPublished = true
            ORDER BY p.createdAt DESC
            """)
    List<Portfolio> findLatestPublished();
}
