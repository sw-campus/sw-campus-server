package com.swcampus.domain.comment;

import com.swcampus.domain.notification.Notification;

/**
 * 댓글 생성 및 알림 결과를 담는 DTO
 *
 * @param comment      생성된 댓글
 * @param notification 생성된 알림 (본인 댓글인 경우 null)
 * @param recipientId  알림 수신자 ID
 * @param postId       게시글 ID
 */
public record CommentNotificationResult(
        Comment comment,
        Notification notification,
        Long recipientId,
        Long postId
) {
}
