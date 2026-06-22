package com.backend.common.domain.project.project.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.exception.MemberNotFoundException;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.member.repository.MemberTechStackRepository;
import com.backend.common.domain.notification.entity.NotificationType;
import com.backend.common.domain.notification.service.NotificationService;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.project.application.dto.ProjectApplicationCreateRequest;
import com.backend.common.domain.project.application.entity.ProjectApplication;
import com.backend.common.domain.project.application.repository.ProjectApplicationRepository;
import com.backend.common.domain.member.dto.UserResponse;
import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.ProjectStatus;
import com.backend.common.domain.project.enums.RecruitmentStatus;
import com.backend.common.domain.project.exception.ProjectNotFoundException;
import com.backend.common.domain.project.project.dto.*;
import com.backend.common.domain.project.project.entity.*;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import com.backend.common.domain.project.project.repository.ProjectViewRepository;
import com.backend.common.domain.techstack.entity.MemberTechStack;
import com.backend.common.domain.techstack.entity.ProjectTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final ProjectViewRepository projectViewRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final NotificationService notificationService;


    public Page<ProjectResponse> getProjects(
            String search,
            String category,
            String tech,
            String status,
            Pageable pageable
    ) {
        String qSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        ProjectCategory qCategory = toCategoryFilter(category);
        String qTech = (tech != null && !"All".equalsIgnoreCase(tech)) ? tech.trim() : null;
        Set<ProjectStatus> qStatuses = toStatusFilters(status);

        Page<Project> projectPage = projectRepository.searchProjects(
                qSearch,
                qCategory,
                qTech,
                qStatuses,
                latestFirst(pageable)
        );

        List<Project> projects = projectPage.getContent();
        Set<Long> featuredProjectIds = featuredProjectIds(projects);
        Set<Long> featuredMemberIds = featuredMemberIds();
        Map<Long, List<ProjectMember>> membersByProject = loadMembersByProject(projects);

        return projectPage.map(project -> toProjectResponse(
                project,
                membersByProject.getOrDefault(project.getId(), List.of()),
                featuredProjectIds.contains(project.getId()),
                featuredMemberIds
        ));
    }

    private ProjectCategory toCategoryFilter(String category) {
        if (category == null || category.isBlank() || "All".equalsIgnoreCase(category)) {
            return null;
        }
        return ProjectCategory.from(category.trim());
    }

    private Set<ProjectStatus> toStatusFilters(String status) {
        if (status == null || status.isBlank() || "All".equalsIgnoreCase(status)) {
            return null;
        }
        return Arrays.stream(status.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(ProjectStatus::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ProjectStatus.class)));
    }

    private Pageable latestFirst(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), latestFirstSort());
    }

    private Sort latestFirstSort() {
        return Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
    }

    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        List<ProjectMember> members = projectMemberRepository.findByProjectIdAndMemberStatus(project.getId(), ProjectMemberStatus.ACTIVE);
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
        return portfolioRepository.findLatestPublished().stream()
                .map(Portfolio::getMember)
                .map(member -> toUserResponse(
                        member,
                        featuredIds.contains(member.getId())
                ))
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
                .category(req.category())
                .goal(String.join(", ", Optional.ofNullable(req.goals()).orElseGet(List::of)))
                .deadline(deadline)
                .positions(Optional.ofNullable(req.positions()).orElseGet(List::of).stream()
                        .map(p -> ProjectPosition.builder()
                                .role(parsePosition(p.role()).name())
                                .total(p.total())
                                .build())
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

    public boolean isProjectLeader(Long projectId, Long memberId) {
        if (memberId == null) return false;
        return findActiveProjectMember(projectId, memberId)
                .map(ProjectMember::getRole)
                .filter(role -> role == ProjectRole.LEADER)
                .isPresent();
    }

    public Optional<Long> findPendingApplicationId(Long projectId, Long memberId) {
        if (memberId == null) {
            return Optional.empty();
        }

        return projectApplicationRepository.findPendingApplication(projectId, memberId)
                .map(application -> application.getId());
    }

    @Transactional
    public Long applyProject(Long projectId, Long memberId, ProjectApplicationCreateRequest request) {
        Project project = projectRepository.findById(projectId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));
        Member applicant = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("404", "Member not found"));

        if (!project.isRecruitmentOpen() || project.getStatus() != ProjectStatus.RECRUITING) {
            throw new IllegalStateException("현재 모집 중인 프로젝트에만 지원할 수 있습니다.");
        }
        if (isProjectMember(projectId, memberId)) {
            throw new IllegalStateException("이미 참여 중인 프로젝트입니다.");
        }

        PositionType position = parsePosition(request.position());
        String role = position.name();
        PositionResponse targetPosition = buildPositions(
                project,
                projectMemberRepository.findByProjectId(projectId)
        ).stream()
                .filter(item -> normalizeRole(item.role()).equals(normalizeRole(role)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("모집 중인 포지션이 아닙니다."));

        if (targetPosition.filled() >= targetPosition.total()) {
            throw new IllegalStateException("이미 모집이 완료된 포지션입니다.");
        }

        String message = Optional.ofNullable(request.message()).orElse("").trim();
        if (message.isBlank()) {
            throw new IllegalArgumentException("지원 메시지를 입력해주세요.");
        }

        Optional<ProjectApplication> existingApplication =
                projectApplicationRepository.findByProjectIdAndApplicantId(projectId, memberId);
        if (existingApplication.isPresent()) {
            throw new IllegalStateException("이미 지원한 프로젝트입니다.");
        }

        ProjectApplication application = ProjectApplication.builder()
                .project(project)
                .applicant(applicant)
                .position(position)
                .message(message)
                .build();

        ProjectApplication saveApplication = projectApplicationRepository.save(application);

        Member leader = project.getLeader();
        notificationService.notify(
                leader,
                NotificationType.APPLICATION_RECEIVED,
                "새로운 지원서가 도착했습니다.",
                applicant.getNickname() + "님이 " + project.getTitle() + " 프로젝트에 지원했습니다.",
                "/mypage?tab=proposal",
                saveApplication.getId()
        );

        return saveApplication.getId();
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
        if (req.leaderPosition() == null) {
            throw new IllegalArgumentException("리더 포지션을 선택해주세요.");
        }
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId).stream()
                .filter(member -> member.getMemberStatus() == ProjectMemberStatus.ACTIVE)
                .toList();
        List<ProjectPosition> positions = validateAndBuildPositions(req.positions(), members);
        members.stream()
                .filter(member -> member.getRole() == ProjectRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Project leader not found"))
                .updatePosition(req.leaderPosition());

        project.update(
                req.title().trim(),
                req.description().trim(),
                req.category(),
                String.join(", ", Optional.ofNullable(req.goals()).orElseGet(List::of)),
                deadline,
                req.open(),
                positions
        );
        List<ProjectTechStack> projectTechStacks = buildProjectTechStacks(project, req.techStacks());
        project.updateProjectTechStacks(List.of());
        projectRepository.flush();
        project.updateProjectTechStacks(projectTechStacks);

        return toProjectResponse(project, members, false, featuredMemberIds());
    }

    private List<ProjectTechStack> buildProjectTechStacks(Project project, List<String> techStackNames) {
        return Optional.ofNullable(techStackNames).orElseGet(List::of).stream()
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
                .toList();
    }

    private List<ProjectPosition> validateAndBuildPositions(
            List<PositionUpdateRequest> requests,
            List<ProjectMember> members
    ) {
        List<PositionUpdateRequest> positions = Optional.ofNullable(requests).orElseGet(List::of);
        if (positions.isEmpty()) {
            throw new IllegalArgumentException("모집 포지션을 한 개 이상 입력해주세요.");
        }

        Map<String, Long> filledByRole = members.stream()
                .filter(member -> member.getRole() != ProjectRole.LEADER)
                .collect(Collectors.groupingBy(
                        member -> normalizeRole(member.getPosition().name()),
                        Collectors.counting()
                ));
        Set<String> requestedRoles = new HashSet<>();

        List<ProjectPosition> updatedPositions = positions.stream()
                .map(position -> {
                    String role = position.role() == null ? "" : position.role().trim();
                    if (role.isBlank()) {
                        throw new IllegalArgumentException("모집 포지션명을 입력해주세요.");
                    }

                    String roleCode = parsePosition(role).name();
                    String normalizedRole = normalizeRole(roleCode);
                    if (!requestedRoles.add(normalizedRole)) {
                        throw new IllegalArgumentException("같은 모집 포지션을 중복해서 등록할 수 없습니다.");
                    }

                    int filled = filledByRole.getOrDefault(normalizedRole, 0L).intValue();
                    if (position.total() < Math.max(filled, 1)) {
                        throw new IllegalArgumentException(
                                formatPosition(parsePosition(roleCode)) + " 모집 인원은 현재 참여 인원 " + filled + "명보다 적게 설정할 수 없습니다."
                        );
                    }

                    return ProjectPosition.builder()
                            .role(roleCode)
                            .total(position.total())
                            .build();
                })
                .toList();

        filledByRole.forEach((role, filled) -> {
            if (filled > 0 && !requestedRoles.contains(role)) {
                throw new IllegalArgumentException(
                        "현재 참여 중인 팀원이 있는 포지션은 삭제하거나 이름을 변경할 수 없습니다."
                );
            }
        });

        return updatedPositions;
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
        String leaderRole = members.stream()
                .filter(member -> member.getRole() == ProjectRole.LEADER)
                .findFirst()
                .map(member -> member.getPosition().name())
                .orElse(null);
        UserResponse leader = toUserResponse(
                project.getLeader(),
                featuredMemberIds.contains(project.getLeader().getId()),
                leaderRole
        );
        List<UserResponse> teamMembers = members.stream()
                .filter(member -> member.getRole() != ProjectRole.LEADER)
                .map(member -> toUserResponse(
                        member.getMember(),
                        featuredMemberIds.contains(member.getMember().getId())
                ))
                .toList();

        List<PositionResponse> positions = buildPositions(project, members);

        return new ProjectResponse(
                String.valueOf(project.getId()),
                project.getTitle(),
                project.getDescription(),
                splitGoals(project.getGoal()),
                projectTechStackNames(project),
                positions,
                calculateRecruitmentStatus(project, positions),
                project.getCategory() == null ? inferCategory(project) : project.getCategory(),
                leader,
                teamMembers,
                project.getDeadline().toString(),
                project.getCreatedAt().toLocalDate().toString(),
                featured
        );
    }

    private RecruitmentStatus calculateRecruitmentStatus(Project project, List<PositionResponse> positions) {
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            return RecruitmentStatus.COMPLETED;
        }

        if (project.getStatus() == ProjectStatus.CLOSED) {
            return RecruitmentStatus.CLOSED;
        }

        if (project.getStatus() == ProjectStatus.DISBANDED || project.getStatus() == ProjectStatus.CANCELLED) {
            return RecruitmentStatus.STOPPED;
        }

        if (project.getStatus() == ProjectStatus.RECRUITING) {
            return RecruitmentStatus.RECRUITING;
        }

        if (!project.isRecruitmentOpen()) {
            return RecruitmentStatus.CLOSED;
        }

        return RecruitmentStatus.RECRUITING;
    }

    private UserResponse toUserResponse(Member member, boolean featured) {
        return toUserResponse(member, featured, null);
    }

    private UserResponse toUserResponse(Member member, boolean featured, String roleOverride) {
        Portfolio portfolio = portfolioRepository.findByMemberId(member.getId()).orElse(null);
        List<String> techStack = memberTechStackRepository.findByMemberId(member.getId()).stream()
                .map(MemberTechStack::getTechStack)
                .map(TechStack::getName)
                .toList();

        return new UserResponse(
                String.valueOf(member.getId()),
                member.getNickname(),
                member.getProfileImageUrl(),
                roleOverride != null ? roleOverride : portfolio != null ? portfolio.getDesiredPosition() : "",
                portfolio != null ? portfolio.getIntroduction() : "",
                techStack,
                portfolio != null ? portfolio.getLinks().stream()
                        .filter(l -> "GITHUB".equals(l.getLinkType()))
                        .map(l -> l.getUrl()).findFirst().orElse(null) : null,
                portfolio != null ? portfolio.getLinks().stream()
                        .filter(l -> "DEPLOY".equals(l.getLinkType()))
                        .map(l -> l.getUrl()).findFirst().orElse(null) : null,
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
                .filter(member -> member.getRole() != ProjectRole.LEADER)
                .collect(Collectors.groupingBy(ProjectMember::getPosition, Collectors.counting()));

        if (counts.isEmpty()) {
            int total = recruitmentOpen ? 1 : 0;
            return List.of(new PositionResponse("팀원", 0, total));
        }

        return counts.entrySet().stream()
                .map(entry -> {
                    int filled = entry.getValue().intValue();
                    int total = recruitmentOpen ? filled + 1 : filled;
                    return new PositionResponse(entry.getKey().name(), filled, total);
                })
                .toList();
    }

    private List<PositionResponse> buildPositions(Project project, List<ProjectMember> members) {
        Map<String, Long> filledByRole = members.stream()
                .filter(member -> member.getMemberStatus() == ProjectMemberStatus.ACTIVE)
                .filter(member -> member.getRole() != ProjectRole.LEADER)
                .collect(Collectors.groupingBy(
                        member -> normalizeRole(member.getPosition().name()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        Set<String> includedRoles = new HashSet<>();
        List<PositionResponse> responses = new ArrayList<>();

        for (ProjectPosition position : project.getPositions()) {
            String roleCode = parsePosition(position.getRole()).name();
            String normalizedRole = normalizeRole(roleCode);
            int filled = filledByRole.getOrDefault(normalizedRole, 0L).intValue();
            responses.add(new PositionResponse(roleCode, filled, position.getTotal()));
            includedRoles.add(normalizedRole);
        }

        members.stream()
                .filter(member -> member.getMemberStatus() == ProjectMemberStatus.ACTIVE)
                .filter(member -> member.getRole() != ProjectRole.LEADER)
                .map(ProjectMember::getPosition)
                .distinct()
                .forEach(position -> {
                    String role = position.name();
                    String normalizedRole = normalizeRole(role);
                    if (includedRoles.add(normalizedRole)) {
                        int filled = filledByRole.getOrDefault(normalizedRole, 0L).intValue();
                        responses.add(new PositionResponse(role, filled, filled));
                    }
                });

        return responses;
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private String formatPosition(PositionType position) {
        return position.toRequiredFormat();
    }

    private PositionType parsePosition(String value) {
        String normalized = normalizeRole(value);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("지원 포지션을 선택해주세요.");
        }

        for (PositionType position : PositionType.values()) {
            if (position.name().equalsIgnoreCase(value) ||
                    normalizeRole(formatPosition(position)).equals(normalized)) {
                return position;
            }
        }

        throw new IllegalArgumentException("지원할 수 없는 포지션입니다.");
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


    @Transactional
    public void makeProjectView(Long projectId, Long memberId) {
        // 1. 존재하는 프로젝트와 회원인지 가볍게 영속성 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("404", "Project not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("404", "Member not found"));

        // 💡 [옵션] 동일 유저가 단시간에 새로고침할 때 중복 누적되는 것을 막고 싶다면 존재 여부 체크 가능
        boolean alreadyExists = projectViewRepository.existsByProjectAndMember(project, member);
        if (alreadyExists) {
            return; // 이미 조회 이력이 있다면 기록하지 않고 종료
        }

        // 2. 최근 본 프로젝트(ProjectView) 엔티티 빌딩 및 저장
        ProjectView projectView = ProjectView.builder()
                .project(project)
                .member(member)
                .build();

        projectViewRepository.save(projectView);
    }

    public List<ProjectResponse> convertToResponses(List<Project> projects) {
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

    public ProjectResponse_manage getProject_manage(Long id) {
        Project project = projectRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        List<ProjectMember> members = projectMemberRepository.findByProjectIdAndMemberStatus(project.getId(), ProjectMemberStatus.ACTIVE);
        Set<Long> featuredProjectIds = featuredProjectIds(
                projectRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
        );
        return toProjectResponse_manage(
                project,
                members,
                featuredProjectIds.contains(project.getId()),
                featuredMemberIds()
        );
    }
    private ProjectResponse_manage toProjectResponse_manage(
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
        List<PMResponse> pmResponses = members.stream()
                .filter(member -> member.getRole() != ProjectRole.LEADER)
                .map(member -> toPMResponse(
                        project.getId(),
                        member.getMember(),
                        featuredMemberIds.contains(member.getMember().getId())
                ))
                .toList();
        List<PositionResponse> positions = buildPositions(project, members);
        return new ProjectResponse_manage(
                String.valueOf(project.getId()),
                project.getTitle(),
                project.getDescription(),
                splitGoals(project.getGoal()),
                projectTechStackNames(project),
                positions,
                project.getStatus(),
                project.getCategory() == null ? inferCategory(project) : project.getCategory(),
                leader,
                teamMembers,
                project.getDeadline().toString(),
                project.getCreatedAt().toLocalDate().toString(),
                Math.max(members.size() * 10, 1),
                featured,
                pmResponses
        );
    }
    private PMResponse toPMResponse(Long id, Member member, boolean featured) {
        Portfolio portfolio = portfolioRepository.findByMemberId(member.getId()).orElse(null);
        List<String> techStack = memberTechStackRepository.findByMemberId(member.getId()).stream()
                .map(MemberTechStack::getTechStack)
                .map(TechStack::getName)
                .toList();
        PositionType position = null;
        ProjectRole role = null;
        List<ProjectMember> projectMember = projectMemberRepository.findByProjectId(id);
        for (int i = 0; i < projectMember.size(); i++) {
            if (member.getId() == projectMember.get(i).getMember().getId()) {
                position = projectMember.get(i).getPosition();
                role = projectMember.get(i).getRole();
            }
        }
        return new PMResponse(
                String.valueOf(member.getId()),
                member.getNickname(),
                member.getProfileImageUrl(),
                role,
                portfolio != null ? portfolio.getIntroduction() : "",
                techStack,
                null,
                featured,
                position);
    }

    @Transactional(readOnly = true)
    public List<ApplicantResponse> getProjectApplication(Long projectId) {
        List<ProjectApplication> applications = projectApplicationRepository.getProjectApplicationByProject_Id(projectId);
        return applications.stream().map(app -> {
            Member applicant = app.getApplicant();
            List<String> techStacks = portfolioRepository.findByMemberId(applicant.getId())
                    .map(p -> p.getPortfolioTechStacks().stream()
                            .map(pts -> pts.getTechStack().getName())
                            .toList())
                    .orElse(List.of());
            return new ApplicantResponse(
                    String.valueOf(applicant.getId()),
                    applicant.getNickname(),
                    applicant.getProfileImageUrl(),
                    app.getPosition() != null ? app.getPosition().name() : null,
                    app.getMessage(),
                    techStacks
            );
        }).toList();
    }

    public Project findByID(Long id) {
        return projectRepository.findById(id).get();
    }

    public ProjectMember addMember(Long id, Long projectID) {


        Project project = projectRepository.findById(projectID).get();
        if(project == null){
            throw new RuntimeException("프로젝트 찾기가 실패하였습니다.");
        }
        ProjectApplication projectApplication = projectApplicationRepository.findByProjectIdAndApplicantId(projectID, id).get();
        if(projectApplication==null){
            throw new RuntimeException("지원자 찾기가 실패하였습니다.");
        }

        ProjectMember projectMember = new ProjectMember(project, projectApplication.getApplicant(), projectApplication.getPosition(), ProjectRole.MEMBER);
        try {
            projectMemberRepository.save(projectMember);
            notificationService.notify(
                    projectApplication.getApplicant(),
                    NotificationType.APPLICATION_ACCEPTED,
                    "프로젝트 지원이 수락되었습니다.",
                    "[" + project.getTitle() + "] 프로젝트 팀원으로 합류했습니다!",
                    "/projects/" + project.getId(),
                    project.getId()
            );
            return projectMember;
        } catch (RuntimeException e) {
            throw new RuntimeException("등록되지 못했습니다.");
        }
    }

    public ProjectApplication delMember(Long id, Long projectID) {
        ProjectApplication projectApplication = projectApplicationRepository.findByProjectIdAndApplicantId(projectID, id).get();
        if(projectApplication==null){
            throw new RuntimeException("지원자 찾기가 실패하였습니다.");
        }
        try {
            projectApplication = projectApplicationRepository.deleteProjectApplicationById(projectApplication.getId());
        } catch (Exception e) {
            throw new RuntimeException("지원자 삭제가 실패하였습니다.");
        }
        return projectApplication;
    }

    @Transactional
    public void rejectApplicant(Long projectId, Long applicantMemberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트 찾기가 실패하였습니다."));
        ProjectApplication application = projectApplicationRepository
                .findByProjectIdAndApplicantId(projectId, applicantMemberId)
                .orElseThrow(() -> new RuntimeException("지원자 찾기가 실패하였습니다."));

        application.reject();

        notificationService.notify(
                application.getApplicant(),
                NotificationType.APPLICATION_REJECTED,
                "프로젝트 지원이 거절되었습니다.",
                "[" + project.getTitle() + "] 프로젝트 지원이 아쉽게도 거절되었습니다.",
                "/projects/" + project.getId(),
                project.getId()
        );
    }

    public Project updateProjectByStatus(Long id, ProjectStatus status) {
        Project project = projectRepository.findById(id).get();
        if(project==null){
            throw new RuntimeException("프로젝트 찾기가 실패하였습니다.");
        }
        project.setStatus(status);
        if(status.equals(ProjectStatus.IN_PROGRESS)|| status.equals(ProjectStatus.RECRUITING)){
            project.setRecruitmentOpen(true);
        }else{
            project.setRecruitmentOpen(false);
        }
        try {
            project = projectRepository.save(project);
        } catch (RuntimeException e) {
            throw new RuntimeException("프로젝트 수정이 실패하였습니다.");
        }
        return project;
    }

    @Transactional
    @PreAuthorize("@projectAuthorizer.isProjectLeaderOf(#projectId, authentication.principal.memberId)")
    public void updateStatus(Long projectId, ProjectStatus newStatus) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("404", "프로젝트를 찾을 수 없습니다."));

        project.changeStatus(newStatus);
    }

    @Transactional
    @PreAuthorize("@projectAuthorizer.isProjectLeaderOf(#projectId, authentication.principal.memberId)")
    public void kickProjectMember(Long projectId, Long targetMemberId) {

        // 방출할 대상 팀원이 해당 프로젝트에 실제로 ACTIVE한 상태로 존재하는지 조회
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndMemberIdAndMemberStatus(
                        projectId,
                        targetMemberId,
                        ProjectMemberStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("404", "해당 프로젝트에 참여 중인 활성화된 멤버를 찾을 수 없습니다."));

        projectMember.kickMember();

        // 알림 연동 - 방출당한 사람에게 알림 슛
        notificationService.notify(
                projectMember.getMember(),
                NotificationType.SYSTEM,
                "프로젝트 탈퇴 처리",
                "[" + projectMember.getProject().getTitle() + "] 프로젝트에서 방출되었습니다.",
                null,
                null
        );
    }

    @Transactional
    @PreAuthorize("@projectAuthorizer.isProjectLeaderOf(#projectId, authentication.principal.memberId)")
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("404", "프로젝트를 찾을 수 없습니다."));
        if (project.getStatus() != ProjectStatus.CANCELLED) {
            throw new AccessDeniedException("취소된 프로젝트만 삭제할 수 있습니다.");
        }
        project.setDeletedAt(java.time.LocalDateTime.now());
    }

    @Transactional
    public void leaveProject(Long projectId, Long memberId) {
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndMemberIdAndMemberStatus(
                        projectId,
                        memberId,
                        ProjectMemberStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("404", "해당 프로젝트에 참여 중인 활성화된 멤버를 찾을 수 없습니다."));

        projectMember.leaveTeam();
    }

    @Transactional
    @PreAuthorize("@projectAuthorizer.isProjectLeaderOf(#projectId, authentication.principal.memberId)")
    public void updateMemberRole(Long projectId, Long targetMemberId, ProjectRole newRole) {

        // 권한을 변경할 대상 팀원이 프로젝트에 실제로 존재하는지 조회
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndMemberIdAndMemberStatus(
                        projectId,
                        targetMemberId,
                        ProjectMemberStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("404", "해당 프로젝트에 참여 중인 멤버를 찾을 수 없습니다."));


        projectMember.updateRole(newRole);

        notificationService.notify(
                projectMember.getMember(),
                NotificationType.SYSTEM,
                "프로젝트 권한 변경",
                "[" + projectMember.getProject().getTitle() + "] 프로젝트에서의 권한이 " + newRole.name() + "(으)로 변경되었습니다.",
                "/projects/" + projectId,
                null
        );
    }

}
