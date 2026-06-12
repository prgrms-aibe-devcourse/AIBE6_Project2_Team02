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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MemberRepository memberRepository;
    private final TechStackRepository techStackRepository;
    private final PortfolioTechStackRepository portfolioTechStackRepository;

    public void createPortfolio(Long memberId, PortfolioRequestDto dto)
    {
        Member member = memberRepository.findById(memberId)
                .orElseThrow();



        Portfolio portfolio = Portfolio.create(
                member,
                dto.title(),
                dto.introduction(),
                dto.githubUrl(),
                dto.blogUrl(),
                dto.deployUrl(),
                dto.desiredPosition(),
                dto.isPublished()
        );

        portfolioRepository.save(portfolio);

        List<TechStack> techStacks = techStackRepository.findAllById(dto.techStackIds());

        techStacks.forEach(techStack -> {
            PortfolioTechStack pts = PortfolioTechStack.builder()
                    .portfolio(portfolio)
                    .techStack(techStack)
                    .build();

            portfolioTechStackRepository.save(pts);
        });

    }
}
