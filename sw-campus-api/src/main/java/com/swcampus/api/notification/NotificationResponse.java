package com.swcampus.api.notification;

import com.swcampus.domain.notification.Notification;
import com.swcampus.domain.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "알림 응답")
public class NotificationResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "알림 유형", example = "COMMENT")
    private NotificationType type;

    @Schema(description = "대상 ID (댓글 ID)", example = "123")
    private Long targetId;

    @Schema(description = "게시글 ID", example = "456")
    private Long postId;

    @Schema(description = "발신자 ID", example = "789")
    private Long senderId;

    @Schema(description = "발신자 닉네임", example = "홍길동")
    private String senderNickname;

    @Schema(description = "읽음 여부", example = "false")
    private boolean read;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification, String senderNickname, Long postId) {
        return NotificationResponse.builder()
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
