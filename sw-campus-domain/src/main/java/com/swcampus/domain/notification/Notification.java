package com.swcampus.domain.notification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    private Long id;
    private Long userId;        // 알림 수신자
    private Long senderId;      // 알림 발신자
    private Long targetId;      // 알림 대상 ID (댓글 ID 등)
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;

    public static Notification create(Long userId, Long senderId, Long targetId, NotificationType type) {
        Notification notification = new Notification();
        notification.userId = userId;
        notification.senderId = senderId;
        notification.targetId = targetId;
        notification.type = type;
        notification.read = false;
        notification.createdAt = LocalDateTime.now();
        return notification;
    }

    public static Notification of(Long id, Long userId, Long senderId, Long targetId,
                                   NotificationType type, boolean read, LocalDateTime createdAt) {
        Notification notification = new Notification();
        notification.id = id;
        notification.userId = userId;
        notification.senderId = senderId;
        notification.targetId = targetId;
        notification.type = type;
        notification.read = read;
        notification.createdAt = createdAt;
        return notification;
    }

    public void markAsRead() {
        this.read = true;
    }
}
