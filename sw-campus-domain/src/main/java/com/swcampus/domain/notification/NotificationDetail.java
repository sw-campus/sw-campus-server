package com.swcampus.domain.notification;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림과 연관 정보(발신자 닉네임, 게시글 ID)를 함께 담는 도메인 객체
 */
@Getter
@Builder
public class NotificationDetail {
    private final Long id;
    private final NotificationType type;
    private final Long targetId;
    private final Long postId;
    private final Long senderId;
    private final String senderNickname;
    private final boolean read;
    private final LocalDateTime createdAt;

    public static NotificationDetail of(Notification notification, String senderNickname, Long postId) {
        return NotificationDetail.builder()
                .id(notification.getId())
                .type(notification.getType())
                .targetId(notification.getTargetId())
                .postId(postId)
                .senderId(notification.getSenderId())
                .senderNickname(senderNickname)
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
