package com.backend.common.domain.portfolio.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioCreateRequest;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioUpdateRequest;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.portfolio.portfolio.service.PortfolioService;
import com.backend.common.domain.portfolio.proposals.entity.ProjectProposal;
import com.backend.common.domain.portfolio.proposals.repository.ProjectProposalRepository;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.entity.ProjectMember;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.techstack.entity.PortfolioTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.PortfolioTechStackRepository;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TechStackRepository techStackRepository;

    @Mock
    private PortfolioTechStackRepository portfolioTechStackRepository;

    @Mock private ProjectProposalRepository projectProposalRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;

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
                "https://github.com/test", "https://blog.test.com",
                "https://deploy.test.com", "백엔드 개발자",
                true, List.of(1L, 2L)
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(techStackRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(ts1, ts2));
        given(portfolioRepository.save(any(Portfolio.class))).willAnswer(inv -> inv.getArgument(0));
        given(portfolioTechStackRepository.save(any(PortfolioTechStack.class))).willAnswer(inv -> inv.getArgument(0));

        portfolioService.createPortfolio(1L, dto);

        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
        verify(portfolioTechStackRepository, times(2)).save(any(PortfolioTechStack.class));
    }

    @Test
    @DisplayName("포트폴리오 등록 성공 - 기술 스택 없음")
    void createPortfolio_noTechStacks_success() {
        Member member = mock(Member.class);

        PortfolioCreateRequest dto = new PortfolioCreateRequest(
                "기술 스택 없는 포트폴리오", "소개글",
                null, null, null,
                "프론트엔드 개발자", false, List.of()
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(techStackRepository.findAllById(List.of())).willReturn(List.of());
        given(portfolioRepository.save(any(Portfolio.class))).willAnswer(inv -> inv.getArgument(0));

        portfolioService.createPortfolio(1L, dto);

        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
        verify(portfolioTechStackRepository, never()).save(any());
    }

    @Test
    @DisplayName("포트폴리오 등록 실패 - 존재하지 않는 회원")
    void createPortfolio_memberNotFound_throws() {
        PortfolioCreateRequest dto = new PortfolioCreateRequest(
                "제목", "소개", null, null, null,
                "백엔드", false ,List.of() 
        );

        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.createPortfolio(999L, dto))
                .isInstanceOf(NoSuchElementException.class);

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    @DisplayName("포트폴리오 저장 시 필드값이 DTO와 일치하는지 확인")
    void createPortfolio_portfolioFieldsMatchDto() {
        Member member = mock(Member.class);

        PortfolioCreateRequest dto = new PortfolioCreateRequest(
                "포트폴리오 제목", "소개글 내용",
                "https://github.com/devlink", "https://devlink.blog",
                "https://devlink.deploy", "풀스택 개발자",
                true, List.of()
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(techStackRepository.findAllById(List.of())).willReturn(List.of());

        ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
        given(portfolioRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        portfolioService.createPortfolio(1L, dto);

        Portfolio saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("포트폴리오 제목");
        assertThat(saved.getIntroduction()).isEqualTo("소개글 내용");
        assertThat(saved.getGithubUrl()).isEqualTo("https://github.com/devlink");
        assertThat(saved.getBlogUrl()).isEqualTo("https://devlink.blog");
        assertThat(saved.getDeployUrl()).isEqualTo("https://devlink.deploy");
        assertThat(saved.getDesiredPosition()).isEqualTo("풀스택 개발자");
        assertThat(saved.isPublished()).isTrue();
    }

    // ================= 내 포트폴리오 수정 테스트 =================
    @Test
    @DisplayName("포트폴리오 수정 성공 - 기술 스택 재활용 및 교체")
    void updatePortfolio_success() {
        // given
        Portfolio portfolio = mock(Portfolio.class);
        TechStack existingStack = mock(TechStack.class);
        PortfolioUpdateRequest request = new PortfolioUpdateRequest(
                "수정된 제목", "수정된 소개", "git", "blog", "deploy", "BACKEND", true, List.of("Java", "Spring")
        );

        given(portfolioRepository.findByMemberId(1L)).willReturn(Optional.of(portfolio));
        // Java는 존재해서 재활용, Spring은 신규 저장 시뮬레이션
        given(techStackRepository.findByName("Java")).willReturn(Optional.of(existingStack));
        given(techStackRepository.findByName("Spring")).willReturn(Optional.empty());
        given(techStackRepository.save(any(TechStack.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        portfolioService.updatePortfolio(1L, request);

        // then
        verify(portfolio, times(1)).update("수정된 제목", "수정된 소개", "git", "blog", "deploy", "BACKEND", true);
        verify(techStackRepository, times(1)).save(any(TechStack.class)); // Spring만 세이브되어야 함
        verify(portfolio, times(1)).updateTechStacks(anyList());
    }

    // ================= 프로젝트 제안 처리 테스트 = "BACKEND" 매핑 검증 포함 =================

    @Test
    @DisplayName("프로젝트 제안 수락 성공 - 팀원 테이블에 실시간 추가 적재 확인")
    void handleProposalAction_accept_success() {
        // given
        ProjectProposal proposal = mock(ProjectProposal.class);
        Project project = mock(Project.class);
        Portfolio portfolio = mock(Portfolio.class);
        Member member = mock(Member.class);

        given(projectProposalRepository.findById(100L)).willReturn(Optional.of(proposal));
        given(proposal.getProject()).willReturn(project);
        given(proposal.getPortfolio()).willReturn(portfolio);
        given(portfolio.getMember()).willReturn(member);
        given(portfolio.getDesiredPosition()).willReturn("BACKEND"); // String 반환

        // when
        portfolioService.handleProposalAction(1L, 100L, true);

        // then
        verify(proposal, times(1)).accept(); // 엔티티 캡슐화 메서드 호출 확인
        verify(projectMemberRepository, times(1)).save(any(ProjectMember.class)); // 팀원 빌딩 확인
    }

    @Test
    @DisplayName("프로젝트 제안 거절 성공 - 팀원 추가 없이 상태만 변경")
    void handleProposalAction_reject_success() {
        // given
        ProjectProposal proposal = mock(ProjectProposal.class);
        given(projectProposalRepository.findById(100L)).willReturn(Optional.of(proposal));

        // when
        portfolioService.handleProposalAction(1L, 100L, false);

        // then
        verify(proposal, times(1)).reject();
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("프로젝트 제안 처리 실패 - 존재하지 않는 제안서")
    void handleProposalAction_notFound_throwsException() {
        // given
        given(projectProposalRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> portfolioService.handleProposalAction(1L, 999L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
