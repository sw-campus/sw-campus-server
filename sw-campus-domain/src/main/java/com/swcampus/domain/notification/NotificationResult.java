package com.swcampus.domain.notification;

import lombok.Getter;

import java.util.List;

/**
 * 알림 목록 조회 결과를 담는 도메인 객체
 */
@Getter
public class NotificationResult {
    private final List<NotificationDetail> notifications;
    private final long unreadCount;

    private NotificationResult(List<NotificationDetail> notifications, long unreadCount) {
        this.notifications = notifications;
        this.unreadCount = unreadCount;
    }

    public static NotificationResult of(List<NotificationDetail> notifications, long unreadCount) {
        return new NotificationResult(notifications, unreadCount);
    }
}
