package com.backend.common.domain.portfolio.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.portfolio.dto.PortfolioRequestDto;
import com.backend.common.domain.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.techstack.entity.PortfolioTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.PortfolioTechStackRepository;
import com.backend.common.domain.techstack.repository.TechStackRepository;
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

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    @DisplayName("포트폴리오 등록 성공 - 기술 스택 포함")
    void createPortfolio_success() {
        Member member = mock(Member.class);
        TechStack ts1 = mock(TechStack.class);
        TechStack ts2 = mock(TechStack.class);

        PortfolioRequestDto dto = new PortfolioRequestDto(
                "내 포트폴리오", "소개글입니다",
                "https://github.com/test", "https://blog.test.com",
                "https://deploy.test.com", "백엔드 개발자",
                List.of(1L, 2L), true
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

        PortfolioRequestDto dto = new PortfolioRequestDto(
                "기술 스택 없는 포트폴리오", "소개글",
                null, null, null,
                "프론트엔드 개발자", List.of(), false
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
        PortfolioRequestDto dto = new PortfolioRequestDto(
                "제목", "소개", null, null, null,
                "백엔드", List.of(), false
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

        PortfolioRequestDto dto = new PortfolioRequestDto(
                "포트폴리오 제목", "소개글 내용",
                "https://github.com/devlink", "https://devlink.blog",
                "https://devlink.deploy", "풀스택 개발자",
                List.of(), true
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
}
