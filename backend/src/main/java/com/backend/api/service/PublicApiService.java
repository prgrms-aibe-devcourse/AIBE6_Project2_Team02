package com.backend.api.service;

import com.backend.api.dto.PositionResponse;
import com.backend.api.dto.ProjectResponse;
import com.backend.api.dto.UserResponse;
import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.entity.MemberTechStack;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.member.repository.MemberTechStackRepository;
import com.backend.common.domain.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.project.entity.PositionType;
import com.backend.common.domain.project.entity.Project;
import com.backend.common.domain.project.entity.ProjectMember;
import com.backend.common.domain.project.entity.ProjectRole;
import com.backend.common.domain.project.entity.ProjectStatus;
import com.backend.common.domain.project.repository.ProjectMemberRepository;
import com.backend.common.domain.project.repository.ProjectRepository;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicApiService {

    private static final List<String> POPULAR_TECH_STACKS = List.of(
            "React",
            "TypeScript",
            "Node.js",
            "Python",
            "Next.js",
            "Tailwind CSS",
            "PostgreSQL",
            "AWS",
            "Docker",
            "Figma"
    );

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberRepository memberRepository;
    private final MemberTechStackRepository memberTechStackRepository;
    private final PortfolioRepository portfolioRepository;
    private final TechStackRepository techStackRepository;

    public List<ProjectResponse> getProjects() {
        List<Project> projects = projectRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
        Set<Long> featuredProjectIds = featuredProjectIds(projects);
        Set<Long> featuredMemberIds = featuredMemberIds();
        Map<Long, List<ProjectMember>> membersByProject = loadMembersByProject(projects);

        return projects.stream()
                .map(project -> toProjectResponse(
                        project,
                        membersByProject.getOrDefault(project.getId(), List.of()),
                        featuredProjectIds.contains(project.getId()),
                        featuredMemberIds
                ))
                .toList();
    }

    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        Set<Long> featuredProjectIds = featuredProjectIds(
                projectRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
        );

        return toProjectResponse(
                project,
                members,
                featuredProjectIds.contains(project.getId()),
                featuredMemberIds()
        );
    }

    public List<UserResponse> getMembers() {
        Set<Long> featuredIds = featuredMemberIds();
        return memberRepository.findAll().stream()
                .sorted(Comparator.comparing(Member::getId))
                .map(member -> toUserResponse(member, featuredIds.contains(member.getId())))
                .toList();
    }

    public UserResponse getMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        Set<Long> featuredIds = featuredMemberIds();
        return toUserResponse(member, featuredIds.contains(member.getId()));
    }

    public List<String> getPopularTechStacks() {
        List<String> fromDb = techStackRepository.findAll().stream()
                .map(TechStack::getName)
                .toList();

        if (fromDb.isEmpty()) {
            return POPULAR_TECH_STACKS;
        }

        List<String> ordered = new ArrayList<>();
        for (String name : POPULAR_TECH_STACKS) {
            if (fromDb.contains(name)) {
                ordered.add(name);
            }
        }
        fromDb.stream()
                .filter(name -> !ordered.contains(name))
                .sorted()
                .forEach(ordered::add);
        return ordered;
    }

    private Map<Long, List<ProjectMember>> loadMembersByProject(List<Project> projects) {
        Map<Long, List<ProjectMember>> membersByProject = new HashMap<>();
        for (Project project : projects) {
            membersByProject.put(project.getId(), projectMemberRepository.findByProjectId(project.getId()));
        }
        return membersByProject;
    }

    private Set<Long> featuredProjectIds(List<Project> projects) {
        return projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.RECRUITING && project.isRecruitmentOpen())
                .sorted(Comparator.comparing(Project::getCreatedAt).reversed())
                .limit(4)
                .map(Project::getId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<Long> featuredMemberIds() {
        return memberRepository.findAll().stream()
                .sorted(Comparator.comparing(Member::getId))
                .limit(8)
                .map(Member::getId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private ProjectResponse toProjectResponse(
            Project project,
            List<ProjectMember> members,
            boolean featured,
            Set<Long> featuredMemberIds
    ) {
        UserResponse leader = toUserResponse(
                project.getLeader(),
                featuredMemberIds.contains(project.getLeader().getId())
        );
        List<UserResponse> teamMembers = members.stream()
                .filter(member -> member.getRole() != ProjectRole.LEADER)
                .map(member -> toUserResponse(
                        member.getMember(),
                        featuredMemberIds.contains(member.getMember().getId())
                ))
                .toList();

        return new ProjectResponse(
                String.valueOf(project.getId()),
                project.getTitle(),
                project.getDescription(),
                project.getDescription(),
                splitGoals(project.getGoal()),
                project.getTechStacks() == null ? List.of() : List.copyOf(project.getTechStacks()),
                buildPositions(members, project.isRecruitmentOpen()),
                project.isRecruitmentOpen() ? "Open" : "Closed",
                inferCategory(project),
                leader,
                teamMembers,
                project.getDeadline().toString(),
                project.getCreatedAt().toLocalDate().toString(),
                Math.max(members.size() * 10, 1),
                featured
        );
    }

    private UserResponse toUserResponse(Member member, boolean featured) {
        Portfolio portfolio = portfolioRepository.findByMemberId(member.getId()).orElse(null);
        List<String> techStack = memberTechStackRepository.findByMemberId(member.getId()).stream()
                .map(MemberTechStack::getTechStack)
                .map(TechStack::getName)
                .toList();

        return new UserResponse(
                String.valueOf(member.getId()),
                member.getNickname(),
                member.getProfileImageUrl(),
                portfolio != null ? portfolio.getDesiredPosition() : "",
                portfolio != null ? portfolio.getIntroduction() : "",
                techStack,
                portfolio != null ? portfolio.getGithubUrl() : null,
                portfolio != null ? portfolio.getDeployUrl() : null,
                null,
                featured
        );
    }

    private List<String> splitGoals(String goal) {
        if (goal == null || goal.isBlank()) {
            return List.of();
        }
        return List.of(goal.split(",\\s*"));
    }

    private List<PositionResponse> buildPositions(List<ProjectMember> members, boolean recruitmentOpen) {
        Map<PositionType, Long> counts = members.stream()
                .collect(Collectors.groupingBy(ProjectMember::getPosition, Collectors.counting()));

        if (counts.isEmpty()) {
            int total = recruitmentOpen ? 1 : 0;
            return List.of(new PositionResponse("팀원", 0, total));
        }

        return counts.entrySet().stream()
                .map(entry -> {
                    int filled = entry.getValue().intValue();
                    int total = recruitmentOpen ? filled + 1 : filled;
                    return new PositionResponse(formatPosition(entry.getKey()), filled, total);
                })
                .toList();
    }

    private String formatPosition(PositionType position) {
        return switch (position) {
            case BACKEND -> "백엔드 개발자";
            case FRONTEND -> "프론트엔드 개발자";
            case FULL_STACK -> "풀스택 개발자";
            case DESIGNER -> "디자이너";
            case PRODUCT_MANAGER -> "프로덕트 매니저";
        };
    }

    private String inferCategory(Project project) {
        String text = (project.getTitle() + " " + project.getDescription()).toLowerCase();
        List<String> techStacks = project.getTechStacks() == null ? List.of() : project.getTechStacks();

        if (text.contains("ai") || text.contains("llm") || techStacks.stream().anyMatch(t -> t.toLowerCase().contains("openai"))) {
            return "AI";
        }
        if (text.contains("game") || text.contains("godot") || techStacks.stream().anyMatch(t -> t.equalsIgnoreCase("Godot"))) {
            return "Game";
        }
        if (text.contains("mobile") || text.contains("앱") || techStacks.stream().anyMatch(t ->
                t.equalsIgnoreCase("React Native") || t.equalsIgnoreCase("Flutter") || t.equalsIgnoreCase("Swift"))) {
            return "Mobile";
        }
        if (text.contains("web3") || text.contains("blockchain")) {
            return "Other";
        }
        return "Web";
    }
}
