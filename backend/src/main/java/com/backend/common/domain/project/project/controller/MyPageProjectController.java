package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.service.MyPageProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    @PreAuthorize("isAuthenticated()") // 로그인한 유저인지 검증
    public ResponseEntity<List<Project>> getMyOwnedProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyOwnedProjects(userDetails.getId());
        return ResponseEntity.ok(projects);
    }

    /**
     * 2. 내가 참여중인 프로젝트 목록 조회
     */
    @GetMapping("/participating")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getMyParticipatingProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyParticipatingProjects(userDetails.getId());
        return ResponseEntity.ok(projects);
    }

    /**
     * 3. 내가 지원한 프로젝트 목록 조회
     */
    @GetMapping("/applied")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getMyAppliedProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyAppliedProjects(userDetails.getId());
        return ResponseEntity.ok(projects);
    }

    /**
     * 4. 내가 수행한 프로젝트 목록 조회 (완료/해산)
     */
    @GetMapping("/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getMyCompletedProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyCompletedProjects(userDetails.getId());
        return ResponseEntity.ok(projects);
    }

    /**
     * 5. 내가 최근에 조회한 프로젝트 목록 조회
     */
    @GetMapping("/recent-views")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getMyRecentlyViewedProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Project> projects = myPageProjectService.getMyRecentlyViewedProjects(userDetails.getId());
        return ResponseEntity.ok(projects);
    }
}
