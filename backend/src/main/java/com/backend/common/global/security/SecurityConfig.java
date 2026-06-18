package com.backend.common.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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

                // 🎯 401 및 403 예외 핸들러 설정 연동
                .exceptionHandling(exception -> exception

                        /**
                         * [401 Unauthorized - AuthenticationEntryPoint]
                         * 비로그인(인증 자격 증명이 없는) 상태에서 보호된 자원(/admin, /mypage 등)에 접근할 때 발생하는 예외를 핸들링합니다.
                         * 발생 시 메인 페이지('/')로 리다이렉트합니다.
                         */
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/");
                        })

                        /**
                         * [403 Forbidden - AccessDeniedHandler]
                         * 로그인은 완료되어 인증은 되었으나, 해당 자원에 접근할 수 있는 권한(예: 일반 유저가 /admin 접근)이 없을 때 발생하는 예외를 핸들링합니다.
                         * 발생 시 메인 페이지('/')로 리다이렉트합니다.
                         */
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/");
                        })
                )

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
//        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
//        registration.setEnabled(false);
//        return registration;
//    }
}