package com.backend.common.domain.portfolio.portfolio.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioCreateRequest;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioResponse;
import com.backend.common.domain.portfolio.portfolio.dto.PortfolioUpdateRequest;
import com.backend.common.domain.portfolio.portfolio.entity.Portfolio;
import com.backend.common.domain.portfolio.portfolio.repository.PortfolioRepository;
import com.backend.common.domain.portfolio.proposals.dto.MyPageProposalResponse;
import com.backend.common.domain.portfolio.proposals.entity.ProjectProposal;
import com.backend.common.domain.portfolio.proposals.repository.ProjectProposalRepository;
import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.enums.SelectionStatus;
import com.backend.common.domain.project.project.entity.ProjectMember;
import com.backend.common.domain.project.project.entity.ProjectRole;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.techstack.entity.PortfolioTechStack;
import com.backend.common.domain.techstack.entity.TechStack;
import com.backend.common.domain.techstack.repository.TechStackRepository;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MemberRepository memberRepository;
    private final TechStackRepository techStackRepository;
    private final ProjectProposalRepository projectProposalRepository;
    private final ProjectMemberRepository projectMemberRepository;

    /**
     * 포트폴리오 등록
     */
    @Transactional
    public void createPortfolio(Long memberId, PortfolioCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("404", "존재하지 않는 회원입니다."));

        Portfolio portfolio = Portfolio.create(
                member,
                request.title(),
                request.introduction(),
                request.githubUrl(),
                request.blogUrl(),
                request.deployUrl(),
                request.desiredPosition(),
                request.isPublished()
        );
        portfolioRepository.save(portfolio);

        List<TechStack> techStacks = techStackRepository.findAllById(request.techStackIds());
        List<PortfolioTechStack> portfolioTechStacks = techStacks.stream()
                .map(ts -> PortfolioTechStack.builder().portfolio(portfolio).techStack(ts).build())
                .toList();
        portfolio.updateTechStacks(portfolioTechStacks);
    }

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

        // 2. 기존 기술 스택 삭제 후 flush (INSERT 전 DELETE 보장)
        portfolio.clearTechStacks();
        portfolioRepository.saveAndFlush(portfolio);

        // 3. 새 기술 스택 조회 및 매핑 엔티티 생성
        List<PortfolioTechStack> newTechStacks = request.techStacks().stream()
                .map(stackName -> {
                    TechStack techStack = techStackRepository.findByName(stackName)
                            .orElseGet(() -> techStackRepository.save(new TechStack(stackName)));
                    return PortfolioTechStack.builder()
                            .portfolio(portfolio)
                            .techStack(techStack)
                            .build();
                }).toList();

        // 4. 새 스택 추가
        portfolio.addTechStacks(newTechStacks);

        return PortfolioResponse.from(portfolio);
    }

    public List<MyPageProposalResponse> getMyReceivedProposals(Long memberId) {
        return projectProposalRepository.findMyReceivedProposals(memberId)
                .stream()
                .map(MyPageProposalResponse::from)
                .toList();
    }

    /**
     * 프로젝트 제안 수락 또는 거절 처리
     */
    @Transactional
    @PreAuthorize("@mypageAuthorizer.isProposalRecipient(#proposalId, authentication.principal.memberId)")
    public void handleProposalAction(Long memberId, Long proposalId, boolean isAccept) {

        ProjectProposal proposal = projectProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("404", "존재하지 않는 제안 요청입니다."));

        if (isAccept) {
            proposal.accept();

            PositionType positionEnum = PositionType.valueOf(proposal.getPortfolio().getDesiredPosition());

            ProjectMember projectMember = ProjectMember.builder()
                    .project(proposal.getProject())
                    .member(proposal.getPortfolio().getMember())
                    // 만약 포지션 정보가 필요하다면 현재 기획에 맞춰 주입 (예: 포폴의 희망 직군)
                    .position(positionEnum)
                    .role(ProjectRole.MEMBER)
                    .build();
            projectMemberRepository.save(projectMember);
        } else {
            proposal.reject();
        }
    }

}
