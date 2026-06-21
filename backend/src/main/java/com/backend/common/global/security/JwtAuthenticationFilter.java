package com.backend.common.global.security;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final JsonMapper jsonMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token != null && jwtTokenProvider.isValid(token)) {

            Long memberId = jwtTokenProvider.getMemberId(token);

            Member member = memberRepository.findById(memberId)
                    .orElse(null);

            // 탈퇴했거나 존재하지 않는 회원
            if (member == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 영구정지 회원 체크
            if ("BANNED".equals(member.getStatus())) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 정지 회원 체크
            if ("SUSPENDED".equals(member.getStatus())
                    && member.getSuspensionUntil() != null
                    && member.getSuspensionUntil().isAfter(LocalDateTime.now())) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 관리자 권한 변경 등 최신 정보를 DB 기준으로 사용
            CustomMemberDetails principal =
                    new CustomMemberDetails(
                            member.getId(),
                            member.getNickname(),
                            member.getStatus(),
                            member.getRole().name()
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
