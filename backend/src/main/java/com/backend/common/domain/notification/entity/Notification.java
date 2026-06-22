package com.backend.common.domain.notification.entity;


import com.backend.common.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="receiver_id", nullable = false)
    private Member receiver;        // 알림 받는 사람

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String message;

    private String targetUrl;       // 클릭시 이동할 프론트 경로
    private Long relatedId;         // proposalId / applicationId / reviewId

    private boolean isRead;
    private boolean fcmSent;        // 발송 성공 여부(실패해도 DB 알림은 남겨야 함)

    private LocalDateTime createdAt;

    @Builder
    public Notification(Member receiver, NotificationType type, String title,
                        String message, String targetUrl, Long relatedId)
    {
        this.receiver = receiver;
        this.type = type;
        this.title = title;
        this.message =message;
        this.targetUrl = targetUrl;
        this.relatedId = relatedId;
        this.isRead = false;
        this.fcmSent = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markFcmSent(){
        this.fcmSent = true;
    }
}
