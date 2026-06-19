package com.backend.common.domain.project.project.repository;

import com.backend.common.domain.project.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    List<Project> findByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * 내가 올린 프로젝트 목록 조회
     */
    @Query(value = "SELECT p FROM Project p WHERE p.leader.id = :memberId AND p.deletedAt IS NULL",
            countQuery = "SELECT count(p) FROM Project p WHERE p.leader.id = :memberId AND p.deletedAt IS NULL")
    Page<Project> findMyOwnedProjects(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 내가 참여중인 프로젝트 목록 조회
     */
    @Query(value = "SELECT p FROM Project p " +
            "JOIN ProjectMember pm ON p.id = pm.project.id " +
            "WHERE pm.member.id = :memberId " +
            "AND pm.memberStatus = 'ACTIVE' " +
            "AND pm.isHidden = false " +
            "AND p.status IN ('IN_PROGRESS', 'RECRUITING', 'CLOSED') " +
            "AND p.deletedAt IS NULL",
            countQuery = "SELECT count(p) FROM Project p " +
                    "JOIN ProjectMember pm ON p.id = pm.project.id " +
                    "WHERE pm.member.id = :memberId " +
                    "AND pm.memberStatus = 'ACTIVE' " +
                    "AND pm.isHidden = false " +
                    "AND p.status IN ('IN_PROGRESS', 'RECRUITING', 'CLOSED') " +
                    "AND p.deletedAt IS NULL")
    Page<Project> findMyParticipatingProjects(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 내가 지원한 프로젝트 목록 조회
     */
    @Query(value = "SELECT p FROM Project p " +
            "JOIN ProjectApplication pa ON p.id = pa.project.id " +
            "WHERE pa.applicant.id = :memberId " +
            "AND pa.status = 'PENDING' " +
            "AND p.deletedAt IS NULL",
            countQuery = "SELECT count(p) FROM Project p " +
                    "JOIN ProjectApplication pa ON p.id = pa.project.id " +
                    "WHERE pa.applicant.id = :memberId " +
                    "AND pa.status = 'PENDING' " +
                    "AND p.deletedAt IS NULL")
    Page<Project> findMyAppliedProjects(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 내가 수행한 프로젝트 목록 조회
     */
    @Query(value = "SELECT p FROM Project p " +
            "JOIN ProjectMember pm ON p.id = pm.project.id " +
            "WHERE pm.member.id = :memberId " +
            "AND pm.memberStatus = 'ACTIVE' " +
            "AND pm.isHidden = false " +
            "AND (p.status = 'COMPLETED' OR p.status = 'DISBANDED') " +
            "AND p.deletedAt IS NULL",
            countQuery = "SELECT count(p) FROM Project p " +
                    "JOIN ProjectMember pm ON p.id = pm.project.id " +
                    "WHERE pm.member.id = :memberId " +
                    "AND pm.memberStatus = 'ACTIVE' " +
                    "AND pm.isHidden = false " +
                    "AND (p.status = 'COMPLETED' OR p.status = 'DISBANDED') " +
                    "AND p.deletedAt IS NULL")
    Page<Project> findMyCompletedProjects(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 내가 조회한 프로젝트 목록 조회 - 최근 본 프로젝트
     */
    @Query(value = "SELECT DISTINCT p FROM Project p " +
            "JOIN ProjectView pv ON p.id = pv.project.id " +
            "WHERE pv.member.id = :memberId " +
            "AND p.deletedAt IS NULL",
            countQuery = "SELECT count(DISTINCT p) FROM Project p " +
                    "JOIN ProjectView pv ON p.id = pv.project.id " +
                    "WHERE pv.member.id = :memberId " +
                    "AND p.deletedAt IS NULL")
    Page<Project> findMyRecentlyViewedProjects(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT p.id FROM Project p WHERE LOWER(p.title) LIKE :pattern AND p.deletedAt IS NULL")
    List<Long> findIdsByTitle(@Param("pattern") String pattern);

    List<Project> findByIsHiddenTrueOrderByUpdatedAtDesc();
}
