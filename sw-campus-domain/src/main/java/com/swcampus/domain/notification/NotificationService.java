package com.swcampus.domain.notification;

import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.comment.CommentRepository;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.notification.exception.NotificationAccessDeniedException;
import com.swcampus.domain.notification.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Notification createNotification(Long userId, Long senderId, Long targetId, NotificationType type) {
        // 본인에게 알림을 보내지 않음
        if (userId.equals(senderId)) {
            return null;
        }

        Notification notification = Notification.create(userId, senderId, targetId, type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public NotificationResult getNotificationsWithDetails(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);

        // sender ID 목록 추출 및 Member 조회
        List<Long> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .distinct()
                .toList();

        Map<Long, Member> senderMap = memberRepository.findAllByIds(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        // comment ID 목록 추출 및 Comment 조회 (postId를 가져오기 위해)
        List<Long> commentIds = notifications.stream()
                .map(Notification::getTargetId)
                .distinct()
                .toList();

        Map<Long, Comment> commentMap = commentRepository.findAllByIds(commentIds).stream()
                .collect(Collectors.toMap(Comment::getId, Function.identity()));

        // NotificationDetail 생성
        List<NotificationDetail> details = notifications.stream()
                .map(n -> {
                    Member sender = senderMap.get(n.getSenderId());
                    String nickname = sender != null ? sender.getNickname() : "알 수 없음";
                    Comment comment = commentMap.get(n.getTargetId());
                    Long postId = comment != null ? comment.getPostId() : null;
                    return NotificationDetail.of(n, nickname, postId);
                })
                .toList();

        return NotificationResult.of(details, unreadCount);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException("본인의 알림만 삭제할 수 있습니다.");
        }

        notificationRepository.deleteById(notificationId);
    }
}
