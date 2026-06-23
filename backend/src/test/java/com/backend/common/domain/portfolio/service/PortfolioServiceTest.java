package com.backend.common.domain.portfolio.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.notification.entity.NotificationType;
import com.backend.common.domain.notification.service.NotificationService;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioCreateRequest;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioUpdateRequest;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.entity.PortfolioLink;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.portfolio.portfolio.service.PortfolioService;
import com.backend.common.domain.portfolio.proposals.dto.ProjectProposalCreateRequest;
import com.backend.common.domain.portfolio.proposals.entity.ProjectProposal;
import com.backend.common.domain.portfolio.proposals.repository.ProjectProposalRepository;
import com.backend.common.domain.project.enums.ProjectStatus;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.entity.ProjectMember;
import com.backend.common.domain.project.project.entity.ProjectMemberStatus;
import com.backend.common.domain.project.project.entity.ProjectPosition;
import com.backend.common.domain.project.project.entity.ProjectRole;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.techstack.entity.PortfolioTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock private PortfolioRepository portfolioRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private TechStackRepository techStackRepository;
    @Mock private ProjectProposalRepository projectProposalRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    @DisplayName("포트폴리오 등록 성공 - 기술 스택 포함")
    void createPortfolio_success() {
        Member member = mock(Member.class);
        TechStack ts1 = mock(TechStack.class);
        TechStack ts2 = mock(TechStack.class);

        PortfolioCreateRequest dto = new PortfolioCreateRequest(
                "내 포트폴리오", "소개글입니다",
                "BACKEND",
                List.of(new PortfolioLink("GITHUB", "https://github.com/test")),
                true, List.of(1L, 2L)
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(techStackRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(ts1, ts2));
        given(portfolioRepository.save(any(Portfolio.class))).willAnswer(inv -> inv.getArgument(0));

        portfolioService.createPortfolio(1L, dto);

        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("포트폴리오 등록 성공 - 기술 스택 없음")
    void createPortfolio_noTechStacks_success() {
        Member member = mock(Member.class);

        PortfolioCreateRequest dto = new PortfolioCreateRequest(
                "기술 스택 없는 포트폴리오", "소개글",
                "FRONTEND",
                List.of(),
                false, List.of()
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(techStackRepository.findAllById(List.of())).willReturn(List.of());
        given(portfolioRepository.save(any(Portfolio.class))).willAnswer(inv -> inv.getArgument(0));

        portfolioService.createPortfolio(1L, dto);

        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("포트폴리오 등록 실패 - 존재하지 않는 회원")
    void createPortfolio_memberNotFound_throws() {
        PortfolioCreateRequest dto = new PortfolioCreateRequest(
                "제목", "소개", "BACKEND", List.of(), false, List.of()
        );

        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.createPortfolio(999L, dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("포트폴리오 저장 시 필드값이 DTO와 일치하는지 확인")
    void createPortfolio_portfolioFieldsMatchDto() {
        Member member = mock(Member.class);

        List<PortfolioLink> links = List.of(
                new PortfolioLink("GITHUB", "https://github.com/devlink"),
                new PortfolioLink("BLOG", "https://devlink.blog")
        );

        PortfolioCreateRequest dto = new PortfolioCreateRequest(
                "포트폴리오 제목", "소개글 내용",
                "FULL_STACK", links, true, List.of()
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(techStackRepository.findAllById(List.of())).willReturn(List.of());

        ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
        given(portfolioRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        portfolioService.createPortfolio(1L, dto);

        Portfolio saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("포트폴리오 제목");
        assertThat(saved.getIntroduction()).isEqualTo("소개글 내용");
        assertThat(saved.getDesiredPosition()).isEqualTo("FULL_STACK");
        assertThat(saved.isPublished()).isTrue();
        assertThat(saved.getLinks()).hasSize(2);
        assertThat(saved.getLinks().get(0).getLinkType()).isEqualTo("GITHUB");
        assertThat(saved.getLinks().get(1).getLinkType()).isEqualTo("BLOG");
    }

    @Test
    @DisplayName("포트폴리오 수정 성공 - 기술 스택 재활용 및 교체")
    void updatePortfolio_success() {
        Portfolio portfolio = mock(Portfolio.class);
        TechStack existingStack = mock(TechStack.class);

        List<PortfolioLink> links = List.of(
                new PortfolioLink("GITHUB", "https://github.com/updated")
        );

        PortfolioUpdateRequest request = new PortfolioUpdateRequest(
                "수정된 제목", "수정된 소개", links, "BACKEND", true, List.of("Java", "Spring")
        );

        given(portfolioRepository.findByMemberId(1L)).willReturn(Optional.of(portfolio));
        given(techStackRepository.findByName("Java")).willReturn(Optional.of(existingStack));
        given(techStackRepository.findByName("Spring")).willReturn(Optional.empty());
        given(techStackRepository.save(any(TechStack.class))).willAnswer(inv -> inv.getArgument(0));

        portfolioService.updatePortfolio(1L, request);

        verify(portfolio, times(1)).update("수정된 제목", "수정된 소개", links, "BACKEND", true);
        verify(techStackRepository, times(1)).save(any(TechStack.class));
        verify(portfolio, times(1)).addTechStacks(anyList());
    }

    // ================= 제안 수락/거절 테스트 =================

    @Test
    @DisplayName("제안 수락 성공 - 팀원 추가 및 제안자에게 수락 알림 발송")
    void handleProposalAction_accept_success() {
        ProjectProposal proposal = mock(ProjectProposal.class);
        Project project = mock(Project.class);
        Portfolio portfolio = mock(Portfolio.class);
        Member member = mock(Member.class);
        Member proposer = mock(Member.class);

        given(projectProposalRepository.findById(100L)).willReturn(Optional.of(proposal));
        given(proposal.getProject()).willReturn(project);
        given(proposal.getPortfolio()).willReturn(portfolio);
        given(portfolio.getMember()).willReturn(member);
        given(portfolio.getDesiredPosition()).willReturn("BACKEND");
        given(proposal.getProposer()).willReturn(proposer);
        given(member.getNickname()).willReturn("철수");
        given(project.getId()).willReturn(10L);
        given(proposal.getId()).willReturn(100L);

        portfolioService.handleProposalAction(1L, 100L, true);

        verify(proposal, times(1)).accept();
        verify(projectMemberRepository, times(1)).save(any(ProjectMember.class));
        verify(notificationService, times(1)).notify(
                eq(proposer), eq(NotificationType.PROPOSAL_ACCEPTED),
                anyString(), anyString(), eq("/projects/10"), eq(100L));
    }

    @Test
    @DisplayName("제안 거절 성공 - 팀원 추가 없이 상태 변경 및 제안자에게 거절 알림 발송")
    void handleProposalAction_reject_success() {
        ProjectProposal proposal = mock(ProjectProposal.class);
        Portfolio portfolio = mock(Portfolio.class);
        Member member = mock(Member.class);
        Member proposer = mock(Member.class);

        given(projectProposalRepository.findById(100L)).willReturn(Optional.of(proposal));
        given(proposal.getPortfolio()).willReturn(portfolio);
        given(portfolio.getMember()).willReturn(member);
        given(member.getNickname()).willReturn("철수");
        given(proposal.getProposer()).willReturn(proposer);
        given(proposal.getId()).willReturn(100L);

        portfolioService.handleProposalAction(1L, 100L, false);

        verify(proposal, times(1)).reject();
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
        verify(notificationService, times(1)).notify(
                eq(proposer), eq(NotificationType.PROPOSAL_REJECTED),
                anyString(), anyString(), isNull(), eq(100L));
    }

    @Test
    @DisplayName("제안 처리 예외 - 존재하지 않는 제안서 ID는 ResourceNotFoundException 발생")
    void handleProposalAction_notFound_throwsException() {
        given(projectProposalRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.handleProposalAction(1L, 999L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ================= 제안 생성 테스트 =================

    @Test
    @DisplayName("제안 생성 성공 - 저장 후 포트폴리오 소유자에게 알림 발송")
    void createProjectProposal_success() {
        Portfolio portfolio = mock(Portfolio.class);
        Member portfolioOwner = mock(Member.class);
        ProjectMember proposerMembership = mock(ProjectMember.class);
        Project project = mock(Project.class);
        Member proposer = mock(Member.class);
        ProjectProposal savedProposal = mock(ProjectProposal.class);

        ProjectProposalCreateRequest request = new ProjectProposalCreateRequest(10L, "BACKEND", "같이 하시죠");

        given(portfolioRepository.findByMemberId(2L)).willReturn(Optional.of(portfolio));
        given(portfolio.isPublished()).willReturn(true);
        given(portfolio.getId()).willReturn(50L);
        given(portfolio.getMember()).willReturn(portfolioOwner);
        given(projectMemberRepository.findByProjectIdAndMemberId(10L, 1L)).willReturn(Optional.of(proposerMembership));
        given(proposerMembership.getMemberStatus()).willReturn(ProjectMemberStatus.ACTIVE);
        given(proposerMembership.getRole()).willReturn(ProjectRole.MEMBER);
        given(proposerMembership.getProject()).willReturn(project);
        given(project.getStatus()).willReturn(ProjectStatus.RECRUITING);
        given(project.isRecruitmentOpen()).willReturn(true);
        given(project.getPositions()).willReturn(List.of(ProjectPosition.builder()
                .role("BACKEND")
                .total(1)
                .build()));
        given(projectMemberRepository.findByProjectId(10L)).willReturn(List.of());
        given(projectMemberRepository.existsByProjectIdAndMemberId(10L, 2L)).willReturn(false);
        given(projectProposalRepository.existsByProjectIdAndPortfolioId(10L, 50L)).willReturn(false);
        given(memberRepository.findById(1L)).willReturn(Optional.of(proposer));
        given(proposer.getNickname()).willReturn("영희");
        given(projectProposalRepository.save(any(ProjectProposal.class))).willReturn(savedProposal);
        given(savedProposal.getId()).willReturn(200L);

        portfolioService.createProjectProposal(2L, 1L, request);

        verify(projectProposalRepository, times(1)).save(any(ProjectProposal.class));
        verify(notificationService, times(1)).notify(
                eq(portfolioOwner), eq(NotificationType.PROPOSAL_RECEIVED),
                anyString(), anyString(), eq("/mypage?tab=proposal"), eq(200L));
    }

    @Test
    @DisplayName("제안 생성 실패 - 본인에게 제안 시 IllegalArgumentException 발생")
    void createProjectProposal_selfProposal_throws() {
        ProjectProposalCreateRequest request = new ProjectProposalCreateRequest(10L, "BACKEND", "자기 자신에게 제안");

        assertThatThrownBy(() -> portfolioService.createProjectProposal(1L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인에게");
    }

    @Test
    @DisplayName("제안 생성 예외 - 공개된 포트폴리오가 없으면 ResourceNotFoundException 발생")
    void createProjectProposal_noPublishedPortfolio_throws() {
        ProjectProposalCreateRequest request = new ProjectProposalCreateRequest(10L, "BACKEND", "메시지");

        given(portfolioRepository.findByMemberId(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.createProjectProposal(2L, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
