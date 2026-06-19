package com.backend.common.domain.bookmark.service;

import com.backend.common.domain.bookmark.entity.Bookmark;
import com.backend.common.domain.bookmark.repository.BookmarkRepository;
import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private static final String PROJECT = "PROJECT";
    private static final String PORTFOLIO = "PORTFOLIO";

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;

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
