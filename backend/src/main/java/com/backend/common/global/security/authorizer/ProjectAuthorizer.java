package com.backend.common.global.security.authorizer;

import com.backend.common.domain.project.project.entity.ProjectMemberStatus;
import com.backend.common.domain.project.project.entity.ProjectRole;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component("projectAuthorizer")
@RequiredArgsConstructor
public class ProjectAuthorizer {

    private final ProjectMemberRepository projectMemberRepository;

    /**
     *  특정 프로젝트의 리더인지 검증하는 메서드
     */
    public boolean isProjectLeaderOf(Long projectId, Long memberId) {
        if (projectId == null || memberId == null) {
            return false;
        }

        // 프로젝트에 속해 있고, 역할이 LEADER이며, 현재 ACTIVE 상태인 멤버가 있는지 체크
        return projectMemberRepository.existsByProjectIdAndMemberIdAndRoleAndMemberStatus(
                projectId,
                memberId,
                ProjectRole.LEADER,
                ProjectMemberStatus.ACTIVE
        );
    }
}
