package com.swcampus.api.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "알림 목록 응답")
public class NotificationListResponse {

    @Schema(description = "알림 목록")
    private List<NotificationResponse> notifications;

    @Schema(description = "읽지 않은 알림 개수", example = "5")
    private long unreadCount;

    public static NotificationListResponse of(List<NotificationResponse> notifications, long unreadCount) {
        return NotificationListResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .build();
    }
}
