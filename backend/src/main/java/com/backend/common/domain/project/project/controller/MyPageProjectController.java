package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.service.MyPageProjectService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mypage/projects")
@RequiredArgsConstructor
public class MyPageProjectController {

    private final MyPageProjectService myPageProjectService;

    /**
     * 1. 내가 올린 프로젝트 목록 조회
     */
    @GetMapping("/owned")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<Project>> getMyOwnedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyOwnedProjects(userDetails.getMemberId());
        return RsData.of("200", "내가 올린 프로젝트 목록 조회가 완료되었습니다.", projects);
    }

    /**
     * 2. 내가 참여중인 프로젝트 목록 조회
     */
    @GetMapping("/participating")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<Project>> getMyParticipatingProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyParticipatingProjects(userDetails.getMemberId());
        return RsData.of("200", "내가 참여 중인 프로젝트 목록 조회가 완료되었습니다.", projects);
    }

    /**
     * 3. 내가 지원한 프로젝트 목록 조회
     */
    @GetMapping("/applied")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<Project>> getMyAppliedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyAppliedProjects(userDetails.getMemberId());
        return RsData.of("200", "내가 지원한 프로젝트 목록 조회가 완료되었습니다.", projects);
    }

    /**
     * 4. 내가 수행한 프로젝트 목록 조회 (완료/해산)
     */
    @GetMapping("/completed")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<Project>> getMyCompletedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyCompletedProjects(userDetails.getMemberId());
        return RsData.of("200", "내가 완료/해산한 프로젝트 목록 조회가 완료되었습니다.", projects);
    }

    /**
     * 5. 내가 최근에 조회한 프로젝트 목록 조회
     */
    @GetMapping("/recent-views")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<Project>> getMyRecentlyViewedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyRecentlyViewedProjects(userDetails.getMemberId());
        return RsData.of("200", "최근 본 프로젝트 목록 조회가 완료되었습니다.", projects);
    }
}