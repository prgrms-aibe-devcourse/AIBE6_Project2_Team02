package com.backend.common.domain.project.proposals.repository;

import com.backend.common.domain.project.proposals.entity.ProjectProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectProposalRepository extends JpaRepository<ProjectProposal, Long> {

    /**
     * 내 포폴에 온 제안 목록 조회
     * 조건: 제안 대상 포트폴리오의 주인이 '나(memberId)'이고, 대기(PENDING) 상태인 제안서들
     */
    @Query("SELECT pp FROM ProjectProposal pp " +
            "JOIN FETCH pp.project p " +
            "JOIN FETCH pp.proposer m " +
            "WHERE pp.portfolio.member.id = :memberId " +
            "AND pp.status = 'PENDING' " +
            "ORDER BY pp.createdAt DESC")
    List<ProjectProposal> findMyReceivedProposals(@Param("memberId") Long memberId);
}
