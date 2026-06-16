package com.backend.common.global.init;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.member.repository.MemberTechStackRepository;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.entity.PortfolioLink;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.portfolio.proposals.entity.ProjectProposal;
import com.backend.common.domain.portfolio.proposals.repository.ProjectProposalRepository;
import com.backend.common.domain.project.application.entity.ProjectApplication;
import com.backend.common.domain.project.application.repository.ProjectApplicationRepository;
import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.project.entity.*;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import com.backend.common.domain.project.project.repository.ProjectViewRepository;
import com.backend.common.domain.report.entity.Report;
import com.backend.common.domain.report.enums.ReportReasonType;
import com.backend.common.domain.report.enums.ReportTargetType;
import com.backend.common.domain.report.repository.ReportRepository;
import com.backend.common.domain.review.entity.Review;
import com.backend.common.domain.review.repository.ReviewRepository;
import com.backend.common.domain.techstack.entity.MemberTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DummyDataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final TechStackRepository techStackRepository;
    private final MemberTechStackRepository memberTechStackRepository;
    private final PortfolioRepository portfolioRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;

    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectProposalRepository projectProposalRepository;
    private final ProjectViewRepository projectViewRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean techStacksSeeded = techStackRepository.count() > 0;
        Map<String, TechStack> techStacks = techStacksSeeded ? loadTechStacks() : saveTechStacks();

        if (memberRepository.count() > 0 || projectRepository.count() > 0) {
            return;
        }

        Map<String, MemberSeed> memberSeeds = memberSeeds();
        Map<String, Member> members = saveMembers(memberSeeds);

        saveMemberTechStacks(memberSeeds, members, techStacks);
        savePortfolios(memberSeeds, members);
        saveProjects(members);
        savePeerReviews();
        saveReports(members);

        // 마이페이지 통합 검증용 데이터 세팅
        saveMyPageTestData(members);
    }

    /**
     * 마이페이지 프론트엔드 연동 확인을 위한 테스트 데이터 빌딩
     */
    private void saveMyPageTestData(Map<String, Member> members) {
        Member testUser = Member.create("아무개", "https://avatars.githubusercontent.com/u/12345678?v=4");
        memberRepository.save(testUser);

        members.put("testuser", testUser);

        List<PortfolioLink> testLinks = new ArrayList<>();
        testLinks.add(new PortfolioLink("GITHUB", "https://github.com/test"));
        testLinks.add(new PortfolioLink("BLOG", "https://velog.io/@test"));

        Portfolio testPortfolio = Portfolio.builder()
                .member(testUser)
                .title("안녕하세요, Java 백엔드 개발자 아무개입니다.")
                .introduction("Spring Boot 아키텍처 설계 및 대규모 가용 통합 테스트 작성에 관심이 많습니다.")
                .portfolioLinks(testLinks)
                .desiredPosition("백엔드 개발자")
                .isPublished(true)
                .build();
        portfolioRepository.save(testPortfolio);

        // ----------------------------------------------------
        // [프로젝트 서브 탭 1번] 내가 올린 프로젝트 (OWNED)
        // ----------------------------------------------------
        Project ownedProject = Project.builder()
                .leader(testUser)
                .title("DevLink 리팩토링 및 고도화 스쿼드")
                .description("기존 사이드 프로젝트 모집 플랫폼의 레거시 코드를 제거하고 스프링 부트 4.0으로 마이그레이션할 팀원을 찾습니다.")
                .goal("Next.js App Router 릴리즈 및 모킹 통합 테스트 완공")
                .deadline(LocalDate.now().plusMonths(1))
                .build();
        projectRepository.save(ownedProject);

        ProjectMember ownedLeader = ProjectMember.builder()
                .project(ownedProject)
                .member(testUser)
                .position(PositionType.BACKEND)
                .role(ProjectRole.LEADER)
                .build();
        projectMemberRepository.save(ownedLeader);

        // ----------------------------------------------------
        // [프로젝트 서브 탭 2번] 내가 참여중인 프로젝트 (PARTICIPATING)
        // ----------------------------------------------------
        // 기존에 u1(Alex Chen)이 방장인 프로젝트 가져오기
        List<Project> allProjects = projectRepository.findAll();
        Project participatingProject = allProjects.get(0); // DevLink - 사이드 프로젝트 플랫폼 공고

        ProjectMember activeMember = ProjectMember.builder()
                .project(participatingProject)
                .member(testUser)
                .position(PositionType.BACKEND)
                .role(ProjectRole.MEMBER)
                .build();
        projectMemberRepository.save(activeMember);

        // ----------------------------------------------------
        // [프로젝트 서브 탭 3번] 내가 지원한 프로젝트 (APPLIED)
        // ----------------------------------------------------
        Project targetAppliedProject = allProjects.get(1); // 에코트랙 모바일 앱 공고
        ProjectApplication myApplication = ProjectApplication.builder()
                .project(targetAppliedProject)
                .applicant(testUser)
                .position(PositionType.BACKEND)
                .message("이전에 대용량 트래픽 처리 백엔드 아키텍처를 테스트 코드로 검증한 경험이 있어 팀에 기여하고자 지원합니다!")
                .build();
        // 빌더 생성자 내부 캡슐화 로직에 의해 status는 자동으로 PENDING 상태로 세팅됨
        projectApplicationRepository.save(myApplication);

        // ----------------------------------------------------
        // [프로젝트 서브 탭 4번] 내가 수행한 프로젝트 (COMPLETED)
        // ----------------------------------------------------
        Project completedProject = Project.builder()
                .leader(testUser)
                .title("AIBE 6기 영광의 백엔드 토이 프로젝트")
                .description("MVC 패턴 및 파일 영속화Persistence 구조 설계를 위해 진행했던 11단계 콘솔 기반 명언 앱 서비스입니다.")
                .goal("11단계 명언 파일 관리 시스템 영속화 성공")
                .deadline(LocalDate.now().minusMonths(2))
                .build();
        completedProject.startProject();
        // 엔티티 필드 직접 조작이 불가능하므로 리플렉션 혹은 상태 세팅 비즈니스 메서드가 없다면 강제 변환 명시
        // 만약 엔티티에 완료 상태 변환 메서드가 없다면, 엔티티 필드가 변경 가능하게 리프레시되거나 강제 전이 구조 처리 필요
        projectRepository.save(completedProject);

        ProjectMember completedLeader = ProjectMember.builder()
                .project(completedProject)
                .member(testUser)
                .position(PositionType.BACKEND)
                .role(ProjectRole.LEADER)
                .build();
        projectMemberRepository.save(completedLeader);

        // ----------------------------------------------------
        // [프로젝트 서브 탭 5번] 조회 목록 (RECENT-VIEWS)
        // ----------------------------------------------------
        Project recentViewProject1 = allProjects.get(2); // AI 코드 리뷰어
        Project recentViewProject2 = allProjects.get(3); // 인디 게임: 네온 나이츠

        projectViewRepository.save(ProjectView.builder().member(testUser).project(recentViewProject1).build());
        projectViewRepository.save(ProjectView.builder().member(testUser).project(recentViewProject2).build());

        // ----------------------------------------------------
        // [제안 필터 A] 내 프로젝트에 들어온 지원 (APPLICATIONS)
        // ----------------------------------------------------
        // 내가 올린 프로젝트(ownedProject)에 u2(Sarah Jenkins)와 u6(Lisa Ray)가 각각 백엔드/프론트엔드로 지원
        ProjectApplication incomingApp1 = ProjectApplication.builder()
                .project(ownedProject)
                .applicant(members.get("u2"))
                .position(PositionType.BACKEND)
                .message("파이썬 Django 위주로 개발했지만 이번 기회에 Java Spring Boot 스택을 찐하게 학습하며 참여하고 싶습니다.")
                .build();
        projectApplicationRepository.save(incomingApp1);

        ProjectApplication incomingApp2 = ProjectApplication.builder()
                .project(ownedProject)
                .applicant(members.get("u6"))
                .position(PositionType.FRONTEND)
                .message("Next.js 프론트 레이아웃 및 탭 분리 리팩토링 컴포넌트 마스터입니다. 화면 연동 깔끔하게 처리해 드릴게요!")
                .build();
        projectApplicationRepository.save(incomingApp2);

        // ----------------------------------------------------
        // [제안 필터 B] 내 포트폴리오에 온 제안 (PROPOSALS)
        // ----------------------------------------------------
        // u10(Nina Simone) 파운더가 정용현의 기가 막힌 벨로그 포폴을 보고 본인의 스타트업 팀 합류 스카우트 제안서 발송
        Project scoutProject = allProjects.get(4); // 동네 음식 구조대 공고
        ProjectProposal incomingProposal = ProjectProposal.builder()
                .project(scoutProject)
                .portfolio(testPortfolio)
                .proposer(members.get("u10"))
                .message("아무개 개발자님의 테스트 중심 백엔드 성장 여정 블로그를 인상 깊게 보았습니다. 저희 동네 음식 구조대 MVP 백엔드 총괄 아키텍터로 모시고 싶습니다!")
                .build();
        projectProposalRepository.save(incomingProposal);
    }

    private Map<String, Member> saveMembers(Map<String, MemberSeed> memberSeeds) {
        Map<String, Member> members = new LinkedHashMap<>();
        memberSeeds.forEach((id, seed) -> members.put(id, memberRepository.save(Member.create(seed.name(), seed.avatar()))));
        return members;
    }

    private static final List<String> BASE_TECH_STACKS = List.of(
            "Java", "JavaScript", "TypeScript", "Python", "C#", "PHP", "Kotlin", "Swift", "Go",
            "Spring Boot", "React", "Vue.js", "Angular", "Next.js", "Node.js", "Django",
            "React Native", "Flutter",
            "PostgreSQL", "MySQL", "MongoDB", "Redis",
            "Docker", "Kubernetes", "AWS", "Firebase",
            "Figma", "Tailwind CSS"
    );

    private Map<String, TechStack> saveTechStacks() {
        Map<String, TechStack> techStacks = new LinkedHashMap<>();
        BASE_TECH_STACKS.forEach(name -> techStacks.put(name,
                techStackRepository.save(TechStack.builder().name(name).build())
        ));
        return techStacks;
    }

    private Map<String, TechStack> loadTechStacks() {
        Map<String, TechStack> techStacks = new LinkedHashMap<>();
        techStackRepository.findAll().forEach(ts -> techStacks.put(ts.getName(), ts));
        return techStacks;
    }

    private void saveMemberTechStacks(
            Map<String, MemberSeed> memberSeeds,
            Map<String, Member> members,
            Map<String, TechStack> techStacks
    ) {
        memberSeeds.forEach((id, seed) ->
                seed.techStacks().stream()
                        .filter(techStacks::containsKey)
                        .forEach(techStackName ->
                                memberTechStackRepository.save(
                                        MemberTechStack.builder()
                                                .member(members.get(id))
                                                .techStack(techStacks.get(techStackName))
                                                .build()
                                )
                        )
        );
    }

    private void savePortfolios(Map<String, MemberSeed> memberSeeds, Map<String, Member> members) {
        memberSeeds.forEach((id, seed) -> portfolioRepository.save(
                Portfolio.builder()
                        .member(members.get(id))
                        .title(seed.name() + " Portfolio")
                        .introduction(seed.bio())
                        .portfolioLinks(null)
                        .desiredPosition(seed.role())
                        .isPublished(true)
                        .build()
        ));
    }

    private void saveProjects(Map<String, Member> members) {
        for (ProjectSeed seed : projectSeeds()) {

            Project project = Project.builder()
                    .leader(members.get(seed.leaderId()))
                    .title(seed.title())
                    .description(seed.description())
                    .goal(seed.goal())
                    .deadline(LocalDate.parse(seed.deadline()))
                    .positions(buildProjectPositions(seed))
                    .build();

            if (!seed.open()) {
                project.startProject();
            }

            projectRepository.save(project);

            ProjectMember leaderMember = ProjectMember.builder()
                    .project(project)
                    .member(members.get(seed.leaderId()))
                    .position(inferPosition(memberSeeds().get(seed.leaderId()).role()))
                    .role(ProjectRole.LEADER)
                    .build();

            projectMemberRepository.save(leaderMember);

            seed.memberIds().forEach(memberId -> {
                ProjectMember generalMember = ProjectMember.builder()
                        .project(project)
                        .member(members.get(memberId))
                        .position(inferPosition(memberSeeds().get(memberId).role()))
                        .role(ProjectRole.MEMBER)
                        .build();

                projectMemberRepository.save(generalMember);
            });
        }
    }

    private List<ProjectPosition> buildProjectPositions(ProjectSeed seed) {
        Map<String, MemberSeed> seeds = memberSeeds();
        Map<PositionType, Integer> filledByPosition = new LinkedHashMap<>();
        List<String> projectMemberIds = new ArrayList<>();
        projectMemberIds.add(seed.leaderId());
        projectMemberIds.addAll(seed.memberIds());

        projectMemberIds.forEach(memberId -> {
            MemberSeed memberSeed = seeds.get(memberId);
            if (memberSeed == null) return;
            PositionType position = inferPosition(memberSeed.role());
            filledByPosition.merge(position, 1, Integer::sum);
        });

        List<ProjectPosition> positions = new ArrayList<>();
        filledByPosition.forEach((position, filled) ->
                positions.add(ProjectPosition.builder()
                        .role(formatPosition(position))
                        .total(seed.open() ? filled + 1 : filled)
                        .build())
        );
        return positions;
    }

    private void savePeerReviews() {
        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(project.getId());
            if (projectMembers.size() < 2) continue;

            String content = """
                        {
                          "a1":"소통이 원활했습니다.",
                          "a2":"맡은 역할을 책임감 있게 수행했습니다.",
                          "a3":"다음 프로젝트도 함께하고 싶습니다."
                        }
                        """;

            for (ProjectMember reviewerMember : projectMembers) {
                for (ProjectMember revieweeMember : projectMembers) {
                    if (reviewerMember.getId().equals(revieweeMember.getId())) continue;
                    reviewRepository.save(Review.builder()
                            .project(project)
                            .reviewer(reviewerMember.getMember())
                            .reviewee(revieweeMember.getMember())
                            .content(content)
                            .build());
                }
            }
        }
    }

    private void saveReports(Map<String, Member> members) {
        List<Member> memberList = new ArrayList<>(members.values());
        List<Project> projects = projectRepository.findAll();
        if (memberList.isEmpty() || projects.isEmpty()) return;

        reportRepository.save(Report.builder()
                .reporter(memberList.get(0))
                .targetType(ReportTargetType.PROJECT)
                .targetId(projects.get(0).getId())
                .reasonType(ReportReasonType.SPAM)
                .reasonDetail("스팸성 프로젝트 게시글입니다.")
                .build());

        portfolioRepository.findAll().stream().findFirst().ifPresent(portfolio -> {
            reportRepository.save(Report.builder()
                    .reporter(memberList.get(1))
                    .targetType(ReportTargetType.PORTFOLIO)
                    .targetId(portfolio.getId())
                    .reasonType(ReportReasonType.INAPPROPRIATE_CONTENT)
                    .reasonDetail("부적절한 내용이 포함된 포트폴리오입니다.")
                    .build());
        });
    }

    private PositionType inferPosition(String role) {
        if (role.contains("백엔드")) return PositionType.BACKEND;
        if (role.contains("프론트")) return PositionType.FRONTEND;
        if (role.contains("디자이너") || role.contains("아티스트") || role.contains("사운드")) return PositionType.DESIGNER;
        if (role.contains("프로덕트") || role.contains("매니저") || role.contains("마케터")) return PositionType.PRODUCT_MANAGER;
        return PositionType.FULL_STACK;
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Map<String, MemberSeed> memberSeeds() {
        Map<String, MemberSeed> seeds = new LinkedHashMap<>();
        seeds.put("u1", member("Alex Chen", "풀스택 개발자", "확장 가능한 웹 애플리케이션과 오픈소스 툴을 만드는 데 열정이 있습니다.", "https://github.com/alexchen", "https://alexchen.dev", "React", "TypeScript", "Node.js", "PostgreSQL", "AWS"));
        seeds.put("u2", member("Sarah Jenkins", "백엔드 개발자", "시스템 아키텍처에 관심이 많습니다. API 설계와 데이터베이스 최적화를 좋아합니다.", "https://github.com/sjenkins", "", "Python", "Django", "PostgreSQL", "Redis", "Docker"));
        seeds.put("u3", member("Emma Watson", "프로덕트 디자이너", "직관적이고 아름다운 사용자 경험을 만듭니다.", "", "https://emmaw.design", "Figma", "Framer", "HTML/CSS", "React"));
        seeds.put("u4", member("David Kim", "모바일 개발자", "React Native와 Flutter를 활용한 크로스 플랫폼 앱 개발에 전문성이 있습니다.", "https://github.com/davidk", "", "React Native", "Flutter", "TypeScript", "Firebase"));
        seeds.put("u5", member("Michael Chang", "AI 연구원", "LLM 파인튜닝과 프롬프트 엔지니어링을 연구합니다.", "https://github.com/mchang", "", "Python", "PyTorch", "OpenAI API", "LangChain"));
        seeds.put("u6", member("Lisa Ray", "프론트엔드 개발자", "웹 접근성과 인터랙티브 UI 구현을 중요하게 생각합니다.", "", "https://lisaray.dev", "React", "Vue.js", "Three.js", "Tailwind CSS"));
        seeds.put("u7", member("Tom Hardy", "Python 개발자", "자동화와 개발자 도구를 만드는 백엔드 개발자입니다.", "", "", "Python", "Docker", "GitHub Actions"));
        seeds.put("u8", member("Sam Porter", "게임 디렉터", "인디 게임 기획과 레벨 디자인을 담당합니다.", "", "", "Godot", "GDScript"));
        seeds.put("u9", member("Julia Roberts", "레벨 디자이너", "2D 플랫폼 게임의 레벨과 플레이 흐름을 설계합니다.", "", "", "Godot", "Aseprite"));
        seeds.put("u10", member("Nina Simone", "파운더", "소셜 임팩트 제품을 기획하고 운영합니다.", "", "", "Next.js", "Supabase"));
        seeds.put("u11", member("Chris Evans", "풀스택 개발자", "빠른 MVP 개발과 운영 자동화를 좋아합니다.", "", "", "Next.js", "Supabase", "Tailwind CSS"));
        seeds.put("u12", member("Mark Ruffalo", "풀스택 개발자", "서비스 백오피스와 사용자 기능 구현 경험이 있습니다.", "", "", "Next.js", "PostgreSQL"));
        seeds.put("u13", member("Vitalik B.", "블록체인 개발자", "지갑 연동과 온체인 데이터 시각화에 관심이 많습니다.", "", "", "Ethers.js", "Solidity", "Vue.js"));
        seeds.put("u14", member("Satoshi N.", "Web3 엔지니어", "멀티체인 자산 조회와 Web3 인프라를 다룹니다.", "", "", "Ethers.js", "TypeScript"));
        seeds.put("u15", member("Jordan Park", "풀스택 개발자", "스타트업 환경에서 0에서 1을 만드는 일을 좋아합니다.", "https://github.com/jordanp", "", "Next.js", "TypeScript", "Supabase", "Tailwind CSS", "Vercel"));
        seeds.put("u16", member("Yuna Lee", "UI/UX 디자이너", "디자인 시스템 구축과 모션 디자인에 강점이 있습니다.", "", "https://yunalee.design", "Figma", "Framer", "Protopie", "After Effects"));
        seeds.put("u17", member("Daniel Cho", "백엔드 개발자", "대규모 트래픽 처리와 마이크로서비스 아키텍처 경험이 있습니다.", "https://github.com/danielcho", "", "Go", "Kubernetes", "gRPC", "PostgreSQL", "Kafka"));
        seeds.put("u18", member("Priya Sharma", "AI/데이터 엔지니어", "추천 시스템과 데이터 파이프라인 구현을 전문으로 합니다.", "https://github.com/priyash", "", "Python", "TensorFlow", "Spark", "Airflow", "AWS"));
        seeds.put("u19", member("Marco Rossi", "모바일 개발자", "네이티브 iOS 개발과 앱스토어 배포 경험이 있습니다.", "https://github.com/marcorossi", "", "Swift", "SwiftUI", "Combine", "Firebase"));
        seeds.put("u20", member("한지민", "프론트엔드 개발자", "사용자 중심 인터페이스와 컴포넌트 라이브러리 구축 경험이 있습니다.", "https://github.com/jiminhan", "", "React", "TypeScript", "Storybook", "Emotion"));
        return seeds;
    }

    private MemberSeed member(String name, String role, String bio, String github, String portfolio, String... techStacks) {
        String id = "u" + name.hashCode();
        return new MemberSeed(id, name, "https://i.pravatar.cc/150?u=" + name.replace(" ", "").toLowerCase(), role, bio, github, portfolio, Arrays.asList(techStacks));
    }

    private List<ProjectSeed> projectSeeds() {
        return List.of(
                project("DevLink - 사이드 프로젝트 플랫폼", "개발자들이 사이드 프로젝트를 찾고 팀원을 모집할 수 있는 플랫폼입니다.", "MVP 출시, 초기 사용자 1,000명 확보, AI 기반 프로젝트 매칭 구현", "2026-07-15", "2026-06-01", true, "u1", List.of("u2"), List.of("React", "TypeScript", "Tailwind CSS", "Node.js", "PostgreSQL")),
                project("에코트랙(EcoTrack) 모바일 앱", "일상적인 탄소 발자국을 추적하고 줄이는 모바일 애플리케이션입니다.", "크로스 플랫폼 앱 개발, Google Maps API 연동, 게이미피케이션 시스템 설계", "2026-08-01", "2026-05-20", true, "u3", List.of("u4"), List.of("React Native", "Firebase", "TypeScript", "Figma")),
                project("AI 코드 리뷰어", "LLM을 사용하여 PR을 자동으로 리뷰해주는 CLI 도구입니다.", "CLI 인터페이스 구현, GitHub App 연동, 컨텍스트 기반 코드 리뷰 구현", "2026-06-30", "2026-06-05", false, "u5", List.of("u6", "u7"), List.of("Python", "OpenAI API", "GitHub Actions", "Docker")),
                project("인디 게임: 네온 나이츠", "Godot 엔진으로 만드는 사이버펑크 테마의 2D 플랫폼 게임입니다.", "플레이 가능한 10개 레벨 완성, Itch.io 데모 배포, 오리지널 사운드트랙 제작", "2026-09-01", "2026-04-15", true, "u8", List.of("u9"), List.of("Godot", "GDScript", "Aseprite", "FMOD")),
                project("동네 음식 구조대", "남은 음식을 보유한 식당과 지역 봉사자를 연결하는 서비스입니다.", "지역 식당 50곳 파트너십, 실시간 알림 시스템, 봉사자 인증 절차 마련", "2026-05-01", "2026-03-10", false, "u10", List.of("u11", "u12"), List.of("Next.js", "Supabase", "Tailwind CSS", "Vercel")),
                project("멀티체인 포트폴리오 트래커", "여러 체인의 DeFi 자산을 추적하는 프라이버시 중심 대시보드입니다.", "Ethereum, Solana, Polygon 지원, Recharts 기반 차트, 클라이언트 데이터 처리", "2026-07-20", "2026-06-08", true, "u13", List.of("u14"), List.of("Vue.js", "TypeScript", "Ethers.js", "Tailwind CSS")),
                project("소셜 독서 기록 앱 - 북노트", "읽은 책을 기록하고 친구들과 인사이트를 공유하는 서비스입니다.", "MVP 출시, 도서 데이터 API 연동, 소셜 피드와 팔로우 기능", "2026-08-10", "2026-06-09", true, "u20", List.of("u15"), List.of("React", "TypeScript", "Node.js", "MongoDB", "Tailwind CSS")),
                project("실시간 작업 화이트보드", "원격 팀을 위한 무한 캔버스 기반 실시간 협업 화이트보드입니다.", "CRDT 동기화 엔진, 60fps 캔버스 렌더링, 음성/영상 통화 연동", "2026-09-15", "2026-06-07", true, "u6", List.of("u1"), List.of("React", "WebRTC", "Yjs", "Canvas API", "WebSocket")),
                project("AI 면접 코칭 서비스", "음성 인식과 LLM으로 모의 면접 피드백을 제공하는 서비스입니다.", "실시간 음성 분석, LLM 피드백 프롬프트 설계, 직군별 질문 데이터셋", "2026-08-25", "2026-06-04", true, "u18", List.of("u5"), List.of("Python", "OpenAI API", "Whisper", "FastAPI", "React")),
                project("로컬 러닝 크루 매칭 앱", "동네에서 함께 뛸 러닝 메이트를 찾아주는 위치 기반 모바일 앱입니다.", "위치 기반 매칭 알고리즘, 러닝 기록 통계, 그룹 채팅과 일정 관리", "2026-09-30", "2026-05-28", true, "u19", List.of("u4"), List.of("React Native", "TypeScript", "Firebase", "Mapbox")),
                project("오픈소스 디자인 시스템 - Aurora UI", "접근성과 커스터마이징을 우선하는 React 컴포넌트 라이브러리입니다.", "핵심 컴포넌트 30종 구현, Storybook 문서 사이트, 디자인 토큰 시스템", "2026-10-01", "2026-05-15", true, "u16", List.of("u6", "u20"), List.of("React", "TypeScript", "Storybook", "Tailwind CSS", "Figma")),
                project("클라우드 비용 최적화 대시보드", "멀티 클라우드 비용을 분석하고 절감 포인트를 추천하는 FinOps 대시보드입니다.", "클라우드 비용 API 연동, 유휴 리소스 탐지, 비용 추이 시각화", "2026-05-20", "2026-04-02", false, "u17", List.of("u2"), List.of("Go", "React", "PostgreSQL", "Kubernetes", "Recharts"))
        );
    }

    private ProjectSeed project(
            String title,
            String description,
            String goal,
            String deadline,
            String createdAt,
            boolean open,
            String leaderId,
            List<String> memberIds,
            List<String> techStacks
    ) {
        return new ProjectSeed(title, description, goal, deadline, createdAt, open, leaderId, memberIds, techStacks);
    }

    private record MemberSeed(
            String id,
            String name,
            String avatar,
            String role,
            String bio,
            String github,
            String portfolio,
            List<String> techStacks
    ) {
    }

    private record ProjectSeed(
            String title,
            String description,
            String goal,
            String deadline,
            String createdAt,
            boolean open,
            String leaderId,
            List<String> memberIds,
            List<String> techStacks
    ) {
    }
}