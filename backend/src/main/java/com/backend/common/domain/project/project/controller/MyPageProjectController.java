package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.project.dto.MyPageProjectResponse;
import com.backend.common.domain.project.project.dto.MyPageApplicationResponse;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.entity.ProjectPosition;
import com.backend.common.domain.project.project.service.MyPageProjectService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        List<MyPageProjectResponse> responses = myPageProjectService.getMyOwnedProjects(userDetails.getMemberId()) // 🎯 기존 메서드 오타(Participating로 가있던 것)도 정정
                .stream()
                .map(project -> MyPageProjectResponse.from(project, extractPositionEnums(project)))
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
                .map(project -> MyPageProjectResponse.from(project, extractPositionEnums(project))) // 🎯 하드코딩 제거 완료
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
                .map(project -> MyPageProjectResponse.from(project, extractPositionEnums(project))) // 🎯 하드코딩 제거 완료
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
                .map(project -> MyPageProjectResponse.from(project, extractPositionEnums(project))) // 🎯 하드코딩 제거 완료
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
                .map(project -> MyPageProjectResponse.from(project, extractPositionEnums(project))) // 🎯 빈 리스트 나가던 것 동적 변경 완료
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

    /**
     * 내 프로젝트에 온 지원 수락/거절 액션 처리
     */
    @PatchMapping("/applications/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public RsData<Void> handleApplicationAction(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam("accept") boolean accept,
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        myPageProjectService.handleApplicationAction(userDetails.getMemberId(), applicationId, accept);
        String message = accept ? "지원을 수락하여 팀원으로 등록했습니다." : "지원을 거절했습니다.";
        return RsData.of("200", message, null);
    }

    /**
     * 내가 신청한 프로젝트 지원 취소
     */
    @PatchMapping("/applications/{applicationId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public RsData<Void> cancelProjectApplication(
            @PathVariable("applicationId") Long projectId,
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ) {
        myPageProjectService.cancelProjectApplication(userDetails.getMemberId(), projectId);
        return RsData.of("200", "프로젝트 지원 신청이 성공적으로 취소되었습니다.", null);
    }

    // ================= 공통 헬퍼 메서드 =================
    /**
     * 프로젝트 엔티티로부터 수집된 포지션 문자열(value)을 이넘(저넘ㅋ)으로 정제해주는  메서드
     */
    private List<PositionType> extractPositionEnums(Project project) {
        return project.getPositions().stream()
                .map(ProjectPosition::getRole)
                .map(PositionType::fromDescriptionOrCode)
                .distinct()
                .toList();
    }
}