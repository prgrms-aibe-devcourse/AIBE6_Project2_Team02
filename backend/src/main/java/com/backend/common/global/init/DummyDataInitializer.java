package com.backend.common.global.init;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.entity.MemberTechStack;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.member.repository.MemberTechStackRepository;
import com.backend.common.domain.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.project.project.entity.*;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.count() > 0 || projectRepository.count() > 0) {
            return;
        }

        Map<String, MemberSeed> memberSeeds = memberSeeds();
        Map<String, Member> members = saveMembers(memberSeeds);
        Map<String, TechStack> techStacks = saveTechStacks(memberSeeds, projectSeeds());

        saveMemberTechStacks(memberSeeds, members, techStacks);
        savePortfolios(memberSeeds, members);
        saveProjects(members);
    }

    private Map<String, Member> saveMembers(Map<String, MemberSeed> memberSeeds) {
        Map<String, Member> members = new LinkedHashMap<>();
        memberSeeds.forEach((id, seed) -> members.put(id, memberRepository.save(Member.create(seed.name(), seed.avatar()))));
        return members;
    }

    private Map<String, TechStack> saveTechStacks(Map<String, MemberSeed> memberSeeds, List<ProjectSeed> projectSeeds) {
        Map<String, TechStack> techStacks = new LinkedHashMap<>();
        memberSeeds.values().forEach(seed -> putTechStacks(techStacks, seed.techStacks()));
        projectSeeds.forEach(seed -> putTechStacks(techStacks, seed.techStacks()));
        techStacks.replaceAll((name, ignored) -> techStackRepository.save(TechStack.create(name)));
        return techStacks;
    }

    private void putTechStacks(Map<String, TechStack> techStacks, List<String> names) {
        names.forEach(name -> techStacks.putIfAbsent(name, null));
    }

    private void saveMemberTechStacks(
            Map<String, MemberSeed> memberSeeds,
            Map<String, Member> members,
            Map<String, TechStack> techStacks
    ) {
        memberSeeds.forEach((id, seed) -> seed.techStacks().forEach(techStack ->
                memberTechStackRepository.save(MemberTechStack.create(members.get(id), techStacks.get(techStack)))
        ));
    }

    private void savePortfolios(Map<String, MemberSeed> memberSeeds, Map<String, Member> members) {
        memberSeeds.forEach((id, seed) -> portfolioRepository.save(Portfolio.create(
                members.get(id),
                seed.name() + " Portfolio",
                seed.bio(),
                blankToNull(seed.github()),
                null,
                blankToNull(seed.portfolio()),
                seed.role()
        )));
    }

    private void saveProjects(Map<String, Member> members) {
        for (ProjectSeed seed : projectSeeds()) {
            ProjectStatus status = seed.open() ? ProjectStatus.RECRUITING : ProjectStatus.IN_PROGRESS;
            Project project = projectRepository.save(Project.create(
                    members.get(seed.leaderId()),
                    seed.title(),
                    seed.description(),
                    seed.goal(),
                    LocalDate.parse(seed.deadline()),
                    status,
                    seed.open(),
                    LocalDate.parse(seed.createdAt()).atStartOfDay(),
                    seed.techStacks()
            ));

            projectMemberRepository.save(ProjectMember.create(
                    project,
                    members.get(seed.leaderId()),
                    inferPosition(memberSeeds().get(seed.leaderId()).role()),
                    ProjectRole.LEADER
            ));
            seed.memberIds().forEach(memberId -> projectMemberRepository.save(ProjectMember.create(
                    project,
                    members.get(memberId),
                    inferPosition(memberSeeds().get(memberId).role()),
                    ProjectRole.MEMBER
            )));
        }
    }

    private PositionType inferPosition(String role) {
        if (role.contains("백엔드")) {
            return PositionType.BACKEND;
        }
        if (role.contains("프론트")) {
            return PositionType.FRONTEND;
        }
        if (role.contains("디자이너") || role.contains("아티스트") || role.contains("사운드")) {
            return PositionType.DESIGNER;
        }
        if (role.contains("프로덕트") || role.contains("매니저") || role.contains("마케터")) {
            return PositionType.PRODUCT_MANAGER;
        }
        return PositionType.FULL_STACK;
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
                project("AI 코드 리뷰어", "LLM을 사용하여 PR을 자동으로 리뷰해주는 CLI 도구입니다.", "CLI 인터페이스 구현, GitHub App 연동, 컨텍스트 기반 코드 리뷰 구현", "2026-06-30", "2026-06-05", true, "u5", List.of("u6", "u7"), List.of("Python", "OpenAI API", "GitHub Actions", "Docker")),
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
