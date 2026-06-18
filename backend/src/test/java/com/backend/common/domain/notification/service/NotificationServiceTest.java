package com.backend.common.domain.notification.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.notification.dto.NotificationResponse;
import com.backend.common.domain.notification.entity.Notification;
import com.backend.common.domain.notification.entity.NotificationType;
import com.backend.common.domain.notification.repository.NotificationRepository;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import com.backend.common.global.fcm.FcmSender;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private FcmSender fcmSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 발송 성공 - FCM 토큰이 있으면 푸시까지 전송하고 발송 성공 처리")
    void notify_withFcmToken_success() throws FirebaseMessagingException {
        Member receiver = mock(Member.class);
        given(receiver.getFcmToken()).willReturn("dummy-token");
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));

        notificationService.notify(
                receiver, NotificationType.PROPOSAL_RECEIVED,
                "새로운 프로젝트 제안", "철수님이 제안했습니다.",
                "/mypage?tab=proposal", 1L
        );

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(fcmSender, times(1)).send("dummy-token", "새로운 프로젝트 제안", "철수님이 제안했습니다.");
    }

    @Test
    @DisplayName("알림 발송 성공 - FCM 토큰이 없으면 DB에만 저장하고 푸시는 시도하지 않음")
    void notify_withoutFcmToken_skipsPush() {
        Member receiver = mock(Member.class);
        given(receiver.getFcmToken()).willReturn(null);
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));

        notificationService.notify(
                receiver, NotificationType.PROPOSAL_ACCEPTED,
                "제안이 수락되었습니다", "메시지",
                null, 1L
        );

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verifyNoInteractions(fcmSender);
    }

    @Test
    @DisplayName("FCM 발송이 실패해도 예외가 전파되지 않고 DB 알림은 그대로 유지됨")
    void notify_fcmSendFails_doesNotThrow() throws FirebaseMessagingException {
        Member receiver = mock(Member.class);
        given(receiver.getFcmToken()).willReturn("dummy-token");
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("FCM 서버 오류")).when(fcmSender)
                .send(anyString(), anyString(), anyString());

        assertThatCode(() -> notificationService.notify(
                receiver, NotificationType.REVIEW_RECEIVED,
                "리뷰가 도착했습니다", "메시지",
                null, 1L
        )).doesNotThrowAnyException();

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 저장 시 필드값이 인자와 일치하는지 확인")
    void notify_savedNotificationFieldsMatchArguments() {
        Member receiver = mock(Member.class);
        given(receiver.getFcmToken()).willReturn(null);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        given(notificationRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        notificationService.notify(
                receiver, NotificationType.APPLICATION_RECEIVED,
                "새로운 지원자", "영희님이 지원했습니다.",
                "/projects/1", 5L
        );

        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.APPLICATION_RECEIVED);
        assertThat(saved.getTitle()).isEqualTo("새로운 지원자");
        assertThat(saved.getMessage()).isEqualTo("영희님이 지원했습니다.");
        assertThat(saved.getTargetUrl()).isEqualTo("/projects/1");
        assertThat(saved.getRelatedId()).isEqualTo(5L);
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    @DisplayName("내 알림 목록 조회 - 최신순으로 변환된 응답 반환")
    void getMyNotifications_success() {
        Notification n1 = mock(Notification.class);
        given(n1.getId()).willReturn(1L);
        given(n1.getType()).willReturn(NotificationType.PROPOSAL_RECEIVED);
        given(n1.getTitle()).willReturn("제목1");

        given(notificationRepository.findByReceiverIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .willReturn(List.of(n1));

        List<NotificationResponse> responses = notificationService.getMyNotifications(1L);

        assertThat(responses).hasSize(1);
        verify(notificationRepository, times(1)).findByReceiverIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("안 읽은 알림 개수 조회")
    void getUnreadCount_success() {
        given(notificationRepository.countByReceiverIdAndIsReadFalse(1L)).willReturn(3L);

        long count = notificationService.getUnreadCount(1L);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("알림 읽음 처리 성공 - 본인 알림인 경우")
    void markAsRead_success() {
        Notification notification = mock(Notification.class);
        Member receiver = mock(Member.class);
        given(receiver.getId()).willReturn(1L);
        given(notification.getReceiver()).willReturn(receiver);
        given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

        notificationService.markAsRead(10L, 1L);

        verify(notification, times(1)).markAsRead();
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 본인 알림이 아닌 경우 접근 거부")
    void markAsRead_notOwner_throwsAccessDenied() {
        Notification notification = mock(Notification.class);
        Member receiver = mock(Member.class);
        given(receiver.getId()).willReturn(2L);
        given(notification.getReceiver()).willReturn(receiver);
        given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(10L, 1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(notification, never()).markAsRead();
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 존재하지 않는 알림")
    void markAsRead_notFound_throwsResourceNotFound() {
        given(notificationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
