package com.backend.common.domain.project.project.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.member.repository.MemberTechStackRepository;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.project.dto.PositionResponse;
import com.backend.common.domain.project.dto.ProjectCreateRequest;
import com.backend.common.domain.project.dto.ProjectUpdateRequest;
import com.backend.common.domain.project.dto.ProjectResponse;
import com.backend.common.domain.project.dto.UserResponse;
import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.ProjectStatus;
import com.backend.common.domain.project.enums.RecruitmentStatus;
import com.backend.common.domain.project.project.entity.*;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import com.backend.common.domain.techstack.entity.MemberTechStack;
import com.backend.common.domain.techstack.entity.ProjectTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

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

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest req, Long memberId) {
        Member leader = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
        LocalDate deadline = LocalDate.parse(req.deadline());
        validateDeadline(deadline);

        if (deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("현재 연도 이전의 마감일은 설정할 수 없습니다.");
        }

        if (req.leaderPosition() == null) {
            throw new IllegalArgumentException("리더 포지션을 선택해주세요.");
        }

        Project project = Project.builder()
                .leader(leader)
                .title(req.title())
                .description(req.description())
                .fullDescription(req.fullDescription())
                .category(req.category())
                .goal(String.join(", ", Optional.ofNullable(req.goals()).orElseGet(List::of)))
                .deadline(deadline)
                .positions(Optional.ofNullable(req.positions()).orElseGet(List::of).stream()
                        .map(p -> ProjectPosition.builder().role(p.role()).total(p.total()).build())
                        .toList())
                .build();

        Optional.ofNullable(req.techStacks()).orElseGet(List::of).stream()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .map(name -> techStackRepository.findByName(name)
                        .orElseGet(() -> techStackRepository.save(
                                TechStack.builder().name(name).build()
                        )))
                .map(techStack -> ProjectTechStack.builder()
                        .project(project)
                        .techStack(techStack)
                        .build())
                .forEach(project::addProjectTechStack);

        if (!req.open()) {
            project.toggleRecruitment(false);
        }

        Project savedProject = projectRepository.save(project);

        // 필요 시 ProjectMember 추가 로직도 여기에 작성

        ProjectMember leaderMember = ProjectMember.builder()
                .project(savedProject)
                .member(leader)
                .position(req.leaderPosition())
                .role(ProjectRole.LEADER)
                .build();
        projectMemberRepository.save(leaderMember);

        return toProjectResponse(savedProject, List.of(leaderMember), true, Set.of());
    }

    public boolean canEditProject(Long projectId, Long memberId) {
        if (memberId == null) {
            return false;
        }

        return findActiveProjectMember(projectId, memberId)
                .map(ProjectMember::getRole)
                .filter(role -> role == ProjectRole.LEADER || role == ProjectRole.MANAGER)
                .isPresent();
    }

    public boolean isProjectMember(Long projectId, Long memberId) {
        return memberId != null && findActiveProjectMember(projectId, memberId).isPresent();
    }

    private Optional<ProjectMember> findActiveProjectMember(Long projectId, Long memberId) {
        return projectMemberRepository.findByProjectIdAndMemberId(projectId, memberId)
                .filter(member -> member.getMemberStatus() == ProjectMemberStatus.ACTIVE);
    }

    @Transactional
    public ProjectResponse updateProject(
            Long projectId,
            ProjectUpdateRequest req,
            Long memberId
    ) {
        if (!canEditProject(projectId, memberId)) {
            throw new AccessDeniedException("프로젝트 수정 권한이 없습니다.");
        }

        Project project = projectRepository.findById(projectId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));
        LocalDate deadline = LocalDate.parse(req.deadline());
        validateDeadline(deadline);

        project.update(
                req.title().trim(),
                req.description().trim(),
                req.fullDescription().trim(),
                req.category(),
                String.join(", ", Optional.ofNullable(req.goals()).orElseGet(List::of)),
                deadline,
                req.open()
        );

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return toProjectResponse(project, members, false, featuredMemberIds());
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
                project.getFullDescription() == null ? project.getDescription() : project.getFullDescription(),
                splitGoals(project.getGoal()),
                projectTechStackNames(project),
                buildPositions(members, project.isRecruitmentOpen()),
                project.isRecruitmentOpen() ? RecruitmentStatus.OPEN : RecruitmentStatus.CLOSED,
                project.getCategory() == null ? inferCategory(project) : project.getCategory(),
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

    private void validateDeadline(LocalDate deadline) {
        if (deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "모집 마감일은 오늘보다 이전으로 설정할 수 없습니다."
            );
        }
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

    private ProjectCategory inferCategory(Project project) {
        String text = (project.getTitle() + " " + project.getDescription()).toLowerCase();
        List<String> techStacks = projectTechStackNames(project);

        if (text.contains("ai") || text.contains("llm") || techStacks.stream().anyMatch(t -> t.toLowerCase().contains("openai"))) {
            return ProjectCategory.AI;
        }
        if (text.contains("game") || text.contains("godot") || techStacks.stream().anyMatch(t -> t.equalsIgnoreCase("Godot"))) {
            return ProjectCategory.GAME;
        }
        if (text.contains("mobile") || text.contains("앱") || techStacks.stream().anyMatch(t ->
                t.equalsIgnoreCase("React Native") || t.equalsIgnoreCase("Flutter") || t.equalsIgnoreCase("Swift"))) {
            return ProjectCategory.MOBILE;
        }
        if (text.contains("web3") || text.contains("blockchain")) {
            return ProjectCategory.OTHER;
        }
        return ProjectCategory.WEB;
    }

    private List<String> projectTechStackNames(Project project) {
        return project.getProjectTechStacks().stream()
                .map(projectTechStack -> projectTechStack.getTechStack().getName())
                .toList();
    }
}
