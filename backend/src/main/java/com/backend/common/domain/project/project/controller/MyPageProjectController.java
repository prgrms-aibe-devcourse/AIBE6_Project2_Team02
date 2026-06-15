package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.project.project.dto.MyPageProjectResponse;
import com.backend.common.domain.portfolio.proposals.dto.MyPageProposalResponse;
import com.backend.common.domain.project.project.dto.MyPageApplicationResponse;
import com.backend.common.domain.project.project.service.MyPageProjectService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mypage/projects")
@RequiredArgsConstructor
public class MyPageProjectController {

    private final MyPageProjectService myPageProjectService;

    /**
     * 내가 올린 프로젝트 목록 조회
     */
    @GetMapping("/owned")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<MyPageProjectResponse>> getMyOwnedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<MyPageProjectResponse> responses = myPageProjectService.getMyOwnedProjects(userDetails.getMemberId())
                .stream()
                .map(p -> MyPageProjectResponse.from(p, List.of("BACKEND", "FRONTEND"))) // 예시 포지션 배지
                .toList();
        return RsData.of("200", "내가 올린 프로젝트 목록 조회가 완료되었습니다.", responses);
    }

    /**
     * 내가 참여중인 프로젝트 목록 조회
     */
    @GetMapping("/participating")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<MyPageProjectResponse>> getMyParticipatingProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<MyPageProjectResponse> responses = myPageProjectService.getMyParticipatingProjects(userDetails.getMemberId())
                .stream()
                .map(p -> MyPageProjectResponse.from(p, List.of("BACKEND")))
                .toList();
        return RsData.of("200", "내가 참여 중인 프로젝트 목록 조회가 완료되었습니다.", responses);
    }

    /**
     * 내가 지원한 프로젝트 목록 조회
     */
    @GetMapping("/applied")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<MyPageProjectResponse>> getMyAppliedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<MyPageProjectResponse> responses = myPageProjectService.getMyAppliedProjects(userDetails.getMemberId())
                .stream()
                .map(p -> MyPageProjectResponse.from(p, List.of("FRONTEND")))
                .toList();
        return RsData.of("200", "내가 지원한 프로젝트 목록 조회가 완료되었습니다.", responses);
    }

    /**
     * 내가 수행한 프로젝트 목록 조회 (완료/해산)
     */
    @GetMapping("/completed")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<MyPageProjectResponse>> getMyCompletedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<MyPageProjectResponse> responses = myPageProjectService.getMyCompletedProjects(userDetails.getMemberId())
                .stream()
                .map(p -> MyPageProjectResponse.from(p, List.of("FULL_STACK")))
                .toList();
        return RsData.of("200", "내가 완료/해산한 프로젝트 목록 조회가 완료되었습니다.", responses);
    }

    /**
     * 내가 최근에 조회한 프로젝트 목록 조회
     */
    @GetMapping("/recent-views")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<MyPageProjectResponse>> getMyRecentlyViewedProjects(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<MyPageProjectResponse> responses = myPageProjectService.getMyRecentlyViewedProjects(userDetails.getMemberId())
                .stream()
                .map(p -> MyPageProjectResponse.from(p, List.of()))
                .toList();
        return RsData.of("200", "최근 본 프로젝트 목록 조회가 완료되었습니다.", responses);
    }

    /**
     * 최근 본 프로젝트 내역 개별 삭제 기능 (Hard Delete)
     */
    @DeleteMapping("/recent-views/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public RsData<Void> deleteRecentlyViewedProject(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        myPageProjectService.deleteRecentlyViewedProject(userDetails.getMemberId(), projectId);
        return RsData.of("200", "최근 본 프로젝트 목록에서 삭제되었습니다.", null);
    }


    /**
     * 내가 올린 프로젝트에 들어온 지원 목록 조회
     */
    @GetMapping("/applications")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<MyPageApplicationResponse>> getMyProjectApplications(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        List<MyPageApplicationResponse> responses = myPageProjectService.getMyProjectApplications(userDetails.getMemberId())
                .stream()
                .map(MyPageApplicationResponse::from)
                .toList();
        return RsData.of("200", "내 프로젝트에 들어온 지원 목록 조회가 완료되었습니다.", responses);
    }
}