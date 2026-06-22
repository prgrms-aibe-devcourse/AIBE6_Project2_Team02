package com.backend.common.domain.bookmark.controller;

import com.backend.common.domain.bookmark.service.BookmarkService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping("/projects/{projectId}")
    public RsData<Boolean> isProjectBookmarked(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return RsData.of(
                "200",
                "프로젝트 북마크 조회 성공",
                bookmarkService.isBookmarkedProject(principal.getMemberId(), projectId)
        );
    }

    @PostMapping("/projects/{projectId}")
    public RsData<Boolean> addProjectBookmark(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return RsData.of(
                "200",
                "프로젝트 북마크 추가 성공",
                bookmarkService.addProjectBookmark(principal.getMemberId(), projectId)
        );
    }

    @DeleteMapping("/projects/{projectId}")
    public RsData<Boolean> removeProjectBookmark(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return RsData.of(
                "200",
                "프로젝트 북마크 해제 성공",
                bookmarkService.removeProjectBookmark(principal.getMemberId(), projectId)
        );
    }

    @GetMapping("/portfolios/{memberId}")
    public RsData<Boolean> isPortfolioBookmarked(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return RsData.of(
                "200",
                "포트폴리오 북마크 조회 성공",
                bookmarkService.isBookmarkedPortfolio(principal.getMemberId(), memberId)
        );
    }

    @PostMapping("/portfolios/{memberId}")
    public RsData<Boolean> addPortfolioBookmark(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return RsData.of(
                "200",
                "포트폴리오 북마크 추가 성공",
                bookmarkService.addPortfolioBookmark(principal.getMemberId(), memberId)
        );
    }

    @DeleteMapping("/portfolios/{memberId}")
    public RsData<Boolean> removePortfolioBookmark(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return RsData.of(
                "200",
                "포트폴리오 북마크 해제 성공",
                bookmarkService.removePortfolioBookmark(principal.getMemberId(), memberId)
        );
    }
}
