package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.project.project.service.MyPageProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mypage/projects")
@RequiredArgsConstructor
public class MyPageProjectController {

    private final MyPageProjectService myPageProjectService;
//
//    /**
//     * 1. 내가 올린 프로젝트 목록 조회
//     */
//    @GetMapping("/owned")
//    @PreAuthorize("isAuthenticated()")
//    public RsData<List<Project>> getMyOwnedProjects(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        List<Project> projects = myPageProjectService.getMyOwnedProjects(userDetails.getId());
//        return RsData.of("200", "내가 올린 프로젝트 목록 조회가 완료되었습니다.", projects);
//    }
//
//    /**
//     * 2. 내가 참여중인 프로젝트 목록 조회
//     */
//    @GetMapping("/participating")
//    @PreAuthorize("isAuthenticated()")
//    public RsData<List<Project>> getMyParticipatingProjects(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        List<Project> projects = myPageProjectService.getMyParticipatingProjects(userDetails.getId());
//        return RsData.of("200", "내가 참여 중인 프로젝트 목록 조회가 완료되었습니다.", projects);
//    }
//
//    /**
//     * 3. 내가 지원한 프로젝트 목록 조회
//     */
//    @GetMapping("/applied")
//    @PreAuthorize("isAuthenticated()")
//    public RsData<List<Project>> getMyAppliedProjects(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        List<Project> projects = myPageProjectService.getMyAppliedProjects(userDetails.getId());
//        return RsData.of("200", "내가 지원한 프로젝트 목록 조회가 완료되었습니다.", projects);
//    }
//
//    /**
//     * 4. 내가 수행한 프로젝트 목록 조회 (완료/해산)
//     */
//    @GetMapping("/completed")
//    @PreAuthorize("isAuthenticated()")
//    public RsData<List<Project>> getMyCompletedProjects(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        List<Project> projects = myPageProjectService.getMyCompletedProjects(userDetails.getId());
//        return RsData.of("200", "내가 완료/해산한 프로젝트 목록 조회가 완료되었습니다.", projects);
//    }
//
//    /**
//     * 5. 내가 최근에 조회한 프로젝트 목록 조회
//     */
//    @GetMapping("/recent-views")
//    @PreAuthorize("isAuthenticated()")
//    public RsData<List<Project>> getMyRecentlyViewedProjects(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        List<Project> projects = myPageProjectService.getMyRecentlyViewedProjects(userDetails.getId());
//        return RsData.of("200", "최근 본 프로젝트 목록 조회가 완료되었습니다.", projects);
//    }
}