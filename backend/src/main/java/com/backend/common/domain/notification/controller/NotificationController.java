package com.backend.common.domain.notification.controller;


import com.backend.common.domain.notification.dto.NotificationResponse;
import com.backend.common.domain.notification.service.NotificationService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /*
    *   내 알림 목록 조회
    * */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public RsData<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomMemberDetails userDetails
            ){
        List<NotificationResponse> responses = notificationService.getMyNotifications(userDetails.getMemberId());
        return RsData.of("200","알림 목록 조회가 완료 되었습니다.", responses);
    }

    /**
     * 안 읽은 알림 개수 조회
     */
    @GetMapping("/me/unread-count")
    @PreAuthorize("isAuthenticated()")
    public RsData<Long> getUnreadCount(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ){
        long count = notificationService.getUnreadCount(userDetails.getMemberId());
        return RsData.of("200","안 읽은 알림 개수 조회가 완료 되었습니다.", count);
    }

    /**
     *  알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public RsData<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomMemberDetails useDetails
    ){
        notificationService.markAsRead(notificationId, useDetails.getMemberId());
        return RsData.of("200","알림을 읽음 처리했습니다.");
    }
}
