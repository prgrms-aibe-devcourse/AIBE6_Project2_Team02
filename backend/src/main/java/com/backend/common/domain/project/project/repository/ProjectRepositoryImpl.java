package com.backend.common.domain.project.project.repository;

import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.ProjectStatus;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.entity.QProject;
import com.backend.common.domain.techstack.entity.QProjectTechStack;
import com.backend.common.domain.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QProject project = QProject.project;
    private final QMember member = QMember.member;
    private final QProjectTechStack projectTechStack = QProjectTechStack.projectTechStack;

    @Override
    public Page<Project> searchProjects(
            String search,
            ProjectCategory category,
            String tech,
            Set<ProjectStatus> statuses,
            Pageable pageable
    ) {
        List<Project> content = queryFactory
                .selectFrom(project)
                .distinct()
                .join(project.leader, member)
                .leftJoin(project.projectTechStacks, projectTechStack)
                .where(
                        project.deletedAt.isNull(),
                        project.isHidden.isFalse(),
                        categoryEq(category),
                        statusIn(statuses),
                        techEq(tech),
                        searchCondition(search)
                )
                .orderBy(project.createdAt.desc(), project.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(project.id.countDistinct())
                .from(project)
                .join(project.leader, member)
                .leftJoin(project.projectTechStacks, projectTechStack)
                .where(
                        project.deletedAt.isNull(),
                        project.isHidden.isFalse(),
                        categoryEq(category),
                        statusIn(statuses),
                        techEq(tech),
                        searchCondition(search)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression categoryEq(ProjectCategory category) {
        return category == null ? null : project.category.eq(category);
    }

    private BooleanExpression techEq(String tech) {
        return StringUtils.hasText(tech) ? projectTechStack.techStack.name.eq(tech) : null;
    }

    private BooleanExpression statusIn(Set<ProjectStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }

        return project.status.in(statuses);
    }

    private BooleanExpression searchCondition(String search) {
        if (!StringUtils.hasText(search)) {
            return null;
        }

        return project.title.containsIgnoreCase(search)
                .or(project.description.containsIgnoreCase(search))
                .or(project.goal.containsIgnoreCase(search))
                .or(member.nickname.containsIgnoreCase(search));
    }
}
