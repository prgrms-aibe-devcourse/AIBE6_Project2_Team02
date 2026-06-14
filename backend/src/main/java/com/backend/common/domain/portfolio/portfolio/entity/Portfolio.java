package com.backend.common.domain.portfolio.portfolio.entity;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.techstack.entity.PortfolioTechStack;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private Member member;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioTechStack> portfolioTechStacks = new ArrayList<>();

    private String githubUrl;
    private String blogUrl;
    private String deployUrl;
    private String desiredPosition;
    private boolean isPublished;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public static Portfolio create(
            Member member,
            String title,
            String introduction,
            String githubUrl,
            String blogUrl,
            String deployUrl,
            String desiredPosition
    ) {
        Portfolio portfolio = new Portfolio();
        portfolio.member = member;
        portfolio.title = title;
        portfolio.introduction = introduction;
        portfolio.githubUrl = githubUrl;
        portfolio.blogUrl = blogUrl;
        portfolio.deployUrl = deployUrl;
        portfolio.desiredPosition = desiredPosition;
        portfolio.isPublished = true;
        portfolio.createdAt = LocalDateTime.now();
        portfolio.updatedAt = portfolio.createdAt;
        return portfolio;
    }

    /**
     * 포트폴리오 수정 도메인 메서드 (DDD 패턴)
     */
    public void update(
            String title, String introduction, String githubUrl,
            String blogUrl, String deployUrl, String desiredPosition, boolean isPublished
    ) {
        this.title = title;
        this.introduction = introduction;
        this.githubUrl = githubUrl;
        this.blogUrl = blogUrl;
        this.deployUrl = deployUrl;
        this.desiredPosition = desiredPosition;
        this.isPublished = isPublished;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기술 스택 컬렉션 갱신 메서드
     */
    public void updateTechStacks(List<PortfolioTechStack> newStacks) {
        this.portfolioTechStacks.clear();
        this.portfolioTechStacks.addAll(newStacks);
    }

}
