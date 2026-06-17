package com.backend.common.domain.notification.dto;

import com.backend.common.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse (
        Long id,
        String type,
        String title,
        String message,
        String targetUrl,
        Long relatedId,
        boolean isRead,
        LocalDateTime createAt
){
    public static NotificationResponse from(Notification n){
        return new NotificationResponse(
                n.getId(), n.getType().name(), n.getTitle(),
                n.getMessage(), n.getTargetUrl(), n.getRelatedId(),
                n.isRead(), n.getCreatedAt()
        );
    }
}
