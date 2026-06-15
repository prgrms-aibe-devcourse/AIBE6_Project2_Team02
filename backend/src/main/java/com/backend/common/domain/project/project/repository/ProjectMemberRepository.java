package com.backend.common.domain.project.project.repository;

import com.backend.common.domain.project.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);
}
