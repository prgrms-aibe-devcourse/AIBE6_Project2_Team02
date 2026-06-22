package com.backend.common.domain.bookmark.service;

import com.backend.common.domain.bookmark.entity.Bookmark;
import com.backend.common.domain.bookmark.dto.BookmarkedPortfolioResponse;
import com.backend.common.domain.bookmark.dto.BookmarkedProjectResponse;
import com.backend.common.domain.bookmark.repository.BookmarkRepository;
import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioListResponse;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.dto.ProjectResponse;
import com.backend.common.domain.project.project.service.ProjectService;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private static final String PROJECT = "PROJECT";
    private static final String PORTFOLIO = "PORTFOLIO";

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public boolean isBookmarkedProject(Long memberId, Long projectId) {
        validateProject(projectId);
        return bookmarkRepository.existsTarget(memberId, PROJECT, projectId);
    }

    @Transactional
    public boolean addProjectBookmark(Long memberId, Long projectId) {
        validateProject(projectId);
        if (bookmarkRepository.existsTarget(memberId, PROJECT, projectId)) {
            return true;
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
        bookmarkRepository.save(new Bookmark(member, PROJECT, projectId));
        return true;
    }

    @Transactional
    public boolean removeProjectBookmark(Long memberId, Long projectId) {
        validateProject(projectId);
        bookmarkRepository.findTarget(memberId, PROJECT, projectId)
                .ifPresent(bookmarkRepository::delete);
        return false;
    }

    @Transactional(readOnly = true)
    public boolean isBookmarkedPortfolio(Long memberId, Long targetMemberId) {
        Portfolio portfolio = findPublishedPortfolioByMemberId(targetMemberId);
        return bookmarkRepository.existsTarget(memberId, PORTFOLIO, portfolio.getId());
    }

    @Transactional
    public boolean addPortfolioBookmark(Long memberId, Long targetMemberId) {
        Portfolio portfolio = findPublishedPortfolioByMemberId(targetMemberId);
        if (bookmarkRepository.existsTarget(memberId, PORTFOLIO, portfolio.getId())) {
            return true;
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
        bookmarkRepository.save(new Bookmark(member, PORTFOLIO, portfolio.getId()));
        return true;
    }

    @Transactional
    public boolean removePortfolioBookmark(Long memberId, Long targetMemberId) {
        Portfolio portfolio = findPublishedPortfolioByMemberId(targetMemberId);
        bookmarkRepository.findTarget(memberId, PORTFOLIO, portfolio.getId())
                .ifPresent(bookmarkRepository::delete);
        return false;
    }

    @Transactional(readOnly = true)
    public List<BookmarkedProjectResponse> getBookmarkedProjects(Long memberId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, PROJECT);
        List<Long> projectIds = bookmarks.stream().map(Bookmark::getTargetId).toList();
        Map<Long, ProjectResponse> projectsById = projectService.convertToResponses(
                        projectRepository.findAllById(projectIds).stream()
                                .filter(project -> project.getDeletedAt() == null)
                                .toList()
                ).stream()
                .collect(Collectors.toMap(project -> Long.valueOf(project.id()), Function.identity()));

        return bookmarks.stream()
                .map(bookmark -> {
                    ProjectResponse project = projectsById.get(bookmark.getTargetId());
                    return project == null ? null : new BookmarkedProjectResponse(bookmark.getCreatedAt(), project);
                })
                .filter(response -> response != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookmarkedPortfolioResponse> getBookmarkedPortfolios(Long memberId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, PORTFOLIO);
        List<Long> portfolioIds = bookmarks.stream().map(Bookmark::getTargetId).toList();
        Map<Long, Portfolio> portfoliosById = portfolioRepository.findAllById(portfolioIds).stream()
                .filter(Portfolio::isPublished)
                .collect(Collectors.toMap(Portfolio::getId, Function.identity()));

        return bookmarks.stream()
                .map(bookmark -> {
                    Portfolio portfolio = portfoliosById.get(bookmark.getTargetId());
                    return portfolio == null
                            ? null
                            : new BookmarkedPortfolioResponse(bookmark.getCreatedAt(), PortfolioListResponse.from(portfolio, false));
                })
                .filter(response -> response != null)
                .toList();
    }

    private void validateProject(Long projectId) {
        projectRepository.findById(projectId)
                .filter(project -> project.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));
    }

    private Portfolio findPublishedPortfolioByMemberId(Long memberId) {
        return portfolioRepository.findByMemberId(memberId)
                .filter(portfolio -> portfolio.isPublished())
                .orElseThrow(() -> new NoSuchElementException("Portfolio not found"));
    }
}
