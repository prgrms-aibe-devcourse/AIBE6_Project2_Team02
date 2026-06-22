package com.backend.common.domain.project.project.repository;

import com.backend.common.domain.project.project.entity.ProjectMember;
import com.backend.common.domain.project.project.entity.ProjectMemberStatus;
import com.backend.common.domain.project.project.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId);

    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);

    @Query("""
            SELECT pm
            FROM ProjectMember pm
            JOIN FETCH pm.project p
            WHERE pm.member.id = :memberId
              AND pm.memberStatus = 'ACTIVE'
              AND p.status = 'RECRUITING'
              AND p.recruitmentOpen = true
              AND p.deletedAt IS NULL
            ORDER BY p.createdAt DESC
            """)
    List<ProjectMember> findProposalProjects(@Param("memberId") Long memberId);


    // 프로젝트 ID, 회원 ID, 역할, 활성화 상태를 기준으로 데이터 존재 여부 확인
    boolean existsByProjectIdAndMemberIdAndRoleAndMemberStatus(
            Long projectId,
            Long memberId,
            ProjectRole role,
            ProjectMemberStatus memberStatus
    );

    // 특정 프로젝트에 활성화 상태인 멤버 단건 조회
    Optional<ProjectMember> findByProjectIdAndMemberIdAndMemberStatus(
            Long projectId,
            Long memberId,
            ProjectMemberStatus memberStatus
    );

    // 특정 프로젝트의 특정 상태인 멤버 목록 조회
    List<ProjectMember> findByProjectIdAndMemberStatus(Long projectId, ProjectMemberStatus memberStatus);
}
