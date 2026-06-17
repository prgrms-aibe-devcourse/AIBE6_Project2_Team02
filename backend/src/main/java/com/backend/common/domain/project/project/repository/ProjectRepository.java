package com.backend.common.domain.project.project.repository;

import com.backend.common.domain.project.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {


    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN p.projectTechStacks pts " +
            "WHERE p.deletedAt IS NULL " +
            "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR cast(p.status as string) = :status) " +
            "AND (:tech IS NULL OR pts.techStack.name = :tech)")
    Page<Project> searchProjects(
            @Param("search") String search,
            @Param("category") String category,
            @Param("tech") String tech,
            @Param("status") String status,
            Pageable pageable
    );

    List<Project> findByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * 내가 올린 프로젝트 목록 조회
     * 조건: leader_id가 나이고, 소프트 딜리트(deletedAt) 되지 않은 프로젝트
     */
    @Query("SELECT p FROM Project p WHERE p.leader.id = :memberId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Project> findMyOwnedProjects(@Param("memberId") Long memberId);

    /**
     * 내가 참여중인 프로젝트 목록 조회
     * 조건: project_members에 내 ID가 ACTIVE 상태로 있고, 숨김처리(isHidden)하지 않았으며, 프로젝트 상태가 '진행중'인 것
     */
    @Query("SELECT p FROM Project p " +
            "JOIN ProjectMember pm ON p.id = pm.project.id " +
            "WHERE pm.member.id = :memberId " +
            "AND pm.memberStatus = 'ACTIVE' " +
            "AND pm.isHidden = false " +
            "AND p.status = 'IN_PROGRESS' " +
            "AND p.deletedAt IS NULL")
    List<Project> findMyParticipatingProjects(@Param("memberId") Long memberId);

    /**
     * 내가 지원한 프로젝트 목록 조회
     * 조건: project_applications에 내 ID가 PENDING(대기) 상태로 들어있는 프로젝트
     */
    @Query("SELECT p FROM Project p " +
            "JOIN ProjectApplication pa ON p.id = pa.project.id " +
            "WHERE pa.applicant.id = :memberId " +
            "AND pa.status = 'PENDING' " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY pa.createdAt DESC")
    List<Project> findMyAppliedProjects(@Param("memberId") Long memberId);

    /**
     * 내가 수행한 프로젝트 목록 조회
     * 조건: 내가 ACTIVE 멤버로 참여했던 프로젝트 중 상태가 'COMPLETED'(완료)이거나
     * 리더가 펑 터트렸지만(DISBANDED) 내 이력에는 남겨두고 싶어 숨김(isHidden = false)하지 않은 것
     */
    @Query("SELECT p FROM Project p " +
            "JOIN ProjectMember pm ON p.id = pm.project.id " +
            "WHERE pm.member.id = :memberId " +
            "AND pm.memberStatus = 'ACTIVE' " +
            "AND pm.isHidden = false " +
            "AND (p.status = 'COMPLETED' OR p.status = 'DISBANDED') " +
            "AND p.deletedAt IS NULL")
    List<Project> findMyCompletedProjects(@Param("memberId") Long memberId);

    /**
     * 내가 조회한 프로젝트 목록 조회 (최근 본 프로젝트)
     * 조건: project_views 테이블에서 내가 조회한 이력을 조인하여 최근 본 순서대로 정렬
     */
    @Query("SELECT p FROM Project p " +
            "JOIN ProjectView pv ON p.id = pv.project.id " +
            "WHERE pv.member.id = :memberId " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY pv.viewedAt DESC")
    List<Project> findMyRecentlyViewedProjects(@Param("memberId") Long memberId);

    @Query("SELECT p.id FROM Project p WHERE LOWER(p.title) LIKE :pattern AND p.deletedAt IS NULL")
    List<Long> findIdsByTitle(@Param("pattern") String pattern);
}
