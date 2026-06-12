package com.backend.common.domain.project.project.service;

import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyPageProjectService {

    private final ProjectRepository projectRepository;

    public List<Project> getMyOwnedProjects(Long memberId) {
        return projectRepository.findMyOwnedProjects(memberId);
    }

    public List<Project> getMyParticipatingProjects(Long memberId) {
        return projectRepository.findMyParticipatingProjects(memberId);
    }

    public List<Project> getMyAppliedProjects(Long memberId) {
        return projectRepository.findMyAppliedProjects(memberId);
    }

    public List<Project> getMyCompletedProjects(Long memberId) {
        return projectRepository.findMyCompletedProjects(memberId);
    }

    public List<Project> getMyRecentlyViewedProjects(Long memberId) {
        return projectRepository.findMyRecentlyViewedProjects(memberId);
    }
}
