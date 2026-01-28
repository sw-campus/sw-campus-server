package com.swcampus.api.notification;

import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.notification.NotificationResult;
import com.swcampus.domain.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @Operation(summary = "SSE 연결", description = "실시간 알림을 수신하기 위한 SSE 연결을 맺습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연결 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@CurrentMember MemberPrincipal member) {
        if (member == null) {
            // 인증 실패 시 즉시 완료되는 빈 emitter 반환
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }
        return sseEmitterService.createEmitter(member.memberId());
    }

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<NotificationListResponse> getNotifications(@CurrentMember MemberPrincipal member) {
        NotificationResult result = notificationService.getNotificationsWithDetails(member.memberId());

        List<NotificationResponse> responses = result.getNotifications().stream()
                .map(NotificationResponse::from)
                .toList();

        return ResponseEntity.ok(NotificationListResponse.of(responses, result.getUnreadCount()));
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "읽지 않은 알림 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@CurrentMember MemberPrincipal member) {
        long count = notificationService.getUnreadCount(member.memberId());
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "알림 ID", required = true) @PathVariable("notificationId") Long notificationId) {

        notificationService.markAsRead(notificationId, member.memberId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@CurrentMember MemberPrincipal member) {
        notificationService.markAllAsRead(member.memberId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "알림 ID", required = true) @PathVariable("notificationId") Long notificationId) {

        notificationService.deleteNotification(notificationId, member.memberId());
        return ResponseEntity.noContent().build();
    }
}
