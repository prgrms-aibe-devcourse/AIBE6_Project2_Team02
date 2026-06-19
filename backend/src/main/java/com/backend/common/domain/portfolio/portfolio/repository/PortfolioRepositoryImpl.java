package com.backend.common.domain.portfolio.portfolio.repository;

import com.backend.common.domain.member.entity.QMember;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.entity.QPortfolio;
import com.backend.common.domain.techstack.entity.QPortfolioTechStack;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PortfolioRepositoryImpl implements PortfolioRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QPortfolio portfolio = QPortfolio.portfolio;
    private final QMember member = QMember.member;
    private final QPortfolioTechStack portfolioTechStack = QPortfolioTechStack.portfolioTechStack;

    @Override
    public Page<Portfolio> searchPortfolios(
            String search,
            String role,
            String tech,
            Pageable pageable
    ) {
        List<Portfolio> content = queryFactory
                .selectFrom(portfolio)
                .distinct()
                .join(portfolio.member, member)
                .leftJoin(portfolio.portfolioTechStacks, portfolioTechStack)
                .where(
                        portfolio.isPublished.isTrue(),
                        roleEq(role),
                        techEq(tech),
                        searchCondition(search)
                )
                .orderBy(portfolio.createdAt.desc(), portfolio.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(portfolio.id.countDistinct())
                .from(portfolio)
                .join(portfolio.member, member)
                .leftJoin(portfolio.portfolioTechStacks, portfolioTechStack)
                .where(
                        portfolio.isPublished.isTrue(),
                        roleEq(role),
                        techEq(tech),
                        searchCondition(search)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression roleEq(String role) {
        return StringUtils.hasText(role) ? portfolio.desiredPosition.eq(role) : null;
    }

    private BooleanExpression techEq(String tech) {
        return StringUtils.hasText(tech) ? portfolioTechStack.techStack.name.eq(tech) : null;
    }

    private BooleanExpression searchCondition(String search) {
        if (!StringUtils.hasText(search)) {
            return null;
        }

        return member.nickname.containsIgnoreCase(search)
                .or(portfolio.title.containsIgnoreCase(search))
                .or(portfolio.introduction.containsIgnoreCase(search));
    }
}
