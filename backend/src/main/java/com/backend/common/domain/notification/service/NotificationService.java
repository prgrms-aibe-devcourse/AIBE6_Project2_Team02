package com.backend.common.domain.notification.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.notification.dto.NotificationResponse;
import com.backend.common.domain.notification.entity.Notification;
import com.backend.common.domain.notification.entity.NotificationType;
import com.backend.common.domain.notification.repository.NotificationRepository;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import com.backend.common.global.fcm.FcmSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRespoitory;
    private final FcmSender fcmSender;

    @Transactional
    public void notify(Member recevier, NotificationType type, String title,
                       String message, String targetUrl, Long relatedId) {
        Notification notification = Notification.builder()
                .receiver(recevier)
                .type(type)
                .title(title)
                .message(message)
                .targetUrl(targetUrl)
                .relatedId(relatedId)
                .build();

        notificationRespoitory.save(notification);

        if(recevier.getFcmToken() != null){
            try{
                fcmSender.send(recevier.getFcmToken(), title,message);
                notification.markFcmSent();
            }catch (Exception e){
                log.warn("FCM 발송 실패 : receiverId={}", recevier.getId(), e);
            }
        }
    }

    public List<NotificationResponse> getMyNotifications(Long memberId){
        return notificationRespoitory.findByReceiverIdOrderByCreatedAtDesc(memberId, PageRequest.of(0,10))
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long getUnreadCount(Long memberId){
        return notificationRespoitory.countByReceiverIdAndIsReadFalse(memberId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long memberId){
        Notification notification = notificationRespoitory.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("404","존재하지 않는 알림 입니다."));

        if(!notification.getReceiver().getId().equals(memberId)){
            throw new AccessDeniedException("본인의 알림만 읽음 처리 할 수 있습니다.");
        }

        notification.markAsRead();
    }
}
