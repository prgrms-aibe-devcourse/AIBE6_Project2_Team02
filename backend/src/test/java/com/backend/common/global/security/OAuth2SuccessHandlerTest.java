package com.backend.common.global.security;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.entity.OauthAccount;
import com.backend.common.domain.member.entity.OauthProvider;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.member.repository.OauthAccountRepository;
import com.backend.common.domain.notification.entity.NotificationType;
import com.backend.common.domain.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private MemberRepository memberRepository;
    @Mock private OauthAccountRepository oauthAccountRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Test
    @DisplayName("신규 가입자는 첫 로그인 성공 시 WELCOME 알림을 받는다")
    void onAuthenticationSuccess_newMember_sendsWelcomeNotification() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);

        given(token.getAuthorizedClientRegistrationId()).willReturn("google");
        given(token.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "12345",
                "name", "철수",
                "picture", "http://image.com/pic.png"
        ));

        given(oauthAccountRepository.findByProviderAndProviderMemberId(OauthProvider.GOOGLE, "12345"))
                .willReturn(Optional.empty());
        given(jwtTokenProvider.generateToken(any(), anyString())).willReturn("dummy-jwt");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, token);

        verify(memberRepository, times(1)).save(any(Member.class));
        verify(oauthAccountRepository, times(1)).save(any(OauthAccount.class));
        verify(notificationService, times(1)).notify(
                any(Member.class),
                eq(NotificationType.WELCOME),
                anyString(),
                anyString(),
                eq("/mypage"),
                isNull()
        );
    }

    @Test
    @DisplayName("기존 가입자는 재로그인 시 WELCOME 알림을 받지 않는다")
    void onAuthenticationSuccess_existingMember_doesNotSendWelcomeNotification() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        Member existingMember = Member.create("영희", null);

        given(token.getAuthorizedClientRegistrationId()).willReturn("google");
        given(token.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "99999",
                "name", "영희",
                "picture", "http://image.com/pic2.png"
        ));

        OauthAccount existingAccount = OauthAccount.create(existingMember, OauthProvider.GOOGLE, "99999");
        given(oauthAccountRepository.findByProviderAndProviderMemberId(OauthProvider.GOOGLE, "99999"))
                .willReturn(Optional.of(existingAccount));
        given(jwtTokenProvider.generateToken(any(), anyString())).willReturn("dummy-jwt");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, token);

        verifyNoInteractions(notificationService);
    }
}
