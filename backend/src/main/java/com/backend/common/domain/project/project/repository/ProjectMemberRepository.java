package com.backend.common.domain.project.project.repository;

import com.backend.common.domain.project.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId);

    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);
}
