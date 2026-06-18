package com.backend.common.global.security;

import com.backend.common.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin", "/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/mypage/**").authenticated()
                        .requestMatchers("/oauth2/**", "/login/**", "/auth/**").permitAll()
                        .anyRequest().permitAll()
                )

                .exceptionHandling(exception -> exception

                        /**
                         * [401 Unauthorized - AuthenticationEntryPoint]
                         * 비로그인 상태에서 보호된 자원에 접근할 때 401 상태 코드와 RsData JSON을 반환합니다.
                         */
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.setContentType("application/json;charset=UTF-8");

                            RsData<Void> rsData = RsData.of("F-1", "로그인이 필요한 서비스입니다.");

                            // 풀 패키지명을 사용하여 빈 주입 오류 및 라이브러리 파편화 문제를  차단
                            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(rsData));
                        })

                        /**
                         * [403 Forbidden - AccessDeniedHandler]
                         * 권한이 없는 유저가 관리자 자원에 접근할 때 403 상태 코드와 RsData JSON을 반환합니다.
                         */
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN); // 403
                            response.setContentType("application/json;charset=UTF-8");

                            RsData<Void> rsData = RsData.of("F-2", "해당 기능에 대한 접근 권한이 없습니다.");

                            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(rsData));
                        })
                )

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}