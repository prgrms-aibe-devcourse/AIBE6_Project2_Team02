package com.backend.common.domain.project.project.service;

import com.backend.common.domain.project.application.entity.ProjectApplication;
import com.backend.common.domain.project.application.repository.ProjectApplicationRepository;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import com.backend.common.domain.project.project.repository.ProjectViewRepository;
import com.backend.common.domain.project.proposals.entity.ProjectProposal;
import com.backend.common.domain.project.proposals.repository.ProjectProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyPageProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectProposalRepository projectProposalRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectViewRepository projectViewRepository;

    // ================= 마이페이지 프로젝트 조회 5종 =================

    /**
     * 내가 올린 프로젝트 목록 조회
     */
    public List<Project> getMyOwnedProjects(Long memberId) {
        return projectRepository.findMyOwnedProjects(memberId);
    }

    /**
     * 내가 참여중인 프로젝트 목록 조회
     */
    public List<Project> getMyParticipatingProjects(Long memberId) {
        return projectRepository.findMyParticipatingProjects(memberId);
    }

    /**
     * 내가 지원한 프로젝트 목록 조회
     */
    public List<Project> getMyAppliedProjects(Long memberId) {
        return projectRepository.findMyAppliedProjects(memberId);
    }

    /**
     * 내가 수행한 프로젝트 목록 조회 (완료/해산)
     */
    public List<Project> getMyCompletedProjects(Long memberId) {
        return projectRepository.findMyCompletedProjects(memberId);
    }

    /**
     * 내가 최근에 조회한 프로젝트 목록 조회
     */
    public List<Project> getMyRecentlyViewedProjects(Long memberId) {
        return projectRepository.findMyRecentlyViewedProjects(memberId);
    }

    /**
     * 최근 본 프로젝트 목록 내역 개별 물리 삭제 (Hard Delete)
     */
    @Transactional // 💡 변경(쓰기) 작업이므로 트랜잭션 필수!
    public void deleteRecentlyViewedProject(Long memberId, Long projectId) {
        projectViewRepository.deleteByMemberIdAndProjectId(memberId, projectId);
    }

    // ================= 프로젝트 제안/지원 마이페이지 조회 2종 =================

    /**
     * 내 포폴에 온 제안 목록 조회
     */
    public List<ProjectProposal> getMyReceivedProposals(Long memberId) {
        return projectProposalRepository.findMyReceivedProposals(memberId);
    }

    /**
     * 내가 올린 프로젝트 중 지원이 온 목록 조회
     */
    public List<ProjectApplication> getMyProjectApplications(Long memberId) {
        return projectApplicationRepository.findMyProjectApplications(memberId);
    }
}
