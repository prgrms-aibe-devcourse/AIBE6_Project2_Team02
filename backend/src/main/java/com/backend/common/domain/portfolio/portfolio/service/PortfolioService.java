package com.backend.common.domain.portfolio.portfolio.service;

import com.backend.common.domain.portfolio.portfolio.dto.PortfolioResponse;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioUpdateRequest;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.portfolio.proposals.dto.MyPageProposalResponse;
import com.backend.common.domain.portfolio.proposals.repository.ProjectProposalRepository;
import com.backend.common.domain.techstack.entity.PortfolioTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TechStackRepository techStackRepository;
    private final ProjectProposalRepository projectProposalRepository;

    /**
     * 내 포트폴리오 상세 조회
     */
    public PortfolioResponse getMyPortfolio(Long memberId) {
        Portfolio portfolio = portfolioRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("404", "등록된 포트폴리오가 없습니다."));
        return PortfolioResponse.from(portfolio);
    }

    /**
     * 내 포트폴리오 수정 (스택 동시 갱신)
     */
    @Transactional
    public PortfolioResponse updatePortfolio(Long memberId, PortfolioUpdateRequest request) {
        Portfolio portfolio = portfolioRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("404", "등록된 포트폴리오가 없습니다."));

        // 1. 기본 정보 정보 업데이트
        portfolio.update(
                request.title(), request.introduction(), request.githubUrl(),
                request.blogUrl(), request.deployUrl(), request.desiredPosition(), request.isPublished()
        );

        // 2. 기술 스택 마스터 테이블 조회 및 매핑 엔티티 생성
        List<PortfolioTechStack> newTechStacks = request.techStacks().stream()
                .map(stackName -> {
                    TechStack techStack = techStackRepository.findByName(stackName)
                            .orElseGet(() -> techStackRepository.save(new TechStack(stackName)));
                    return PortfolioTechStack.builder()
                            .portfolio(portfolio)
                            .techStack(techStack)
                            .build();
                }).toList();

        // 3. 엔티티 내부 고유 컬렉션 교체 체제 구동
        portfolio.updateTechStacks(newTechStacks);

        return PortfolioResponse.from(portfolio);
    }

    public List<MyPageProposalResponse> getMyReceivedProposals(Long memberId) {
        return projectProposalRepository.findMyReceivedProposals(memberId)
                .stream()
                .map(MyPageProposalResponse::from)
                .toList();
    }

}
