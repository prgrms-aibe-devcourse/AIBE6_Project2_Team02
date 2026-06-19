package com.backend.common.domain.bookmark.controller;

import com.backend.common.domain.bookmark.service.BookmarkService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
                "프로젝트 북마크 삭제 성공",
                bookmarkService.removeProjectBookmark(principal.getMemberId(), projectId)
        );
    }
}
