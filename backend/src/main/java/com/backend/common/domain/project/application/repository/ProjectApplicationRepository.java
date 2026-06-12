package com.backend.common.domain.project.application.repository;

import com.backend.common.domain.project.application.entity.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {

    /**
     * 내가 올린 프로젝트 중 지원이 온 목록 조회
     * 조건: 프로젝트의 팀 리더가 '나(memberId)'이고, 대기(PENDING) 상태인 지원서들
     */
    @Query("SELECT pa FROM ProjectApplication pa " +
            "JOIN FETCH pa.project p " +
            "JOIN FETCH pa.applicant m " +
            "WHERE p.leader.id = :memberId " +
            "AND pa.status = 'PENDING' " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY pa.createdAt DESC")
    List<ProjectApplication> findMyProjectApplications(@Param("memberId") Long memberId);
}
