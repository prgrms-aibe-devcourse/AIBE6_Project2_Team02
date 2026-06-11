package com.backend.common.domain.portfolio.entity;

import com.backend.common.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
