package com.swcampus.domain.comment;

import com.swcampus.domain.comment.exception.CommentAccessDeniedException;
import com.swcampus.domain.comment.exception.CommentNotFoundException;
import com.swcampus.domain.notification.Notification;
import com.swcampus.domain.notification.NotificationService;
import com.swcampus.domain.notification.NotificationType;
import com.swcampus.domain.post.Post;
import com.swcampus.domain.post.PostRepository;
import com.swcampus.domain.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Transactional
    public Comment createComment(Long postId, Long userId, Long parentId, String body, String imageUrl) {
        Comment comment = Comment.create(postId, userId, parentId, body, imageUrl);
        Comment saved = commentRepository.save(comment);

        // 게시글 댓글 수 증가
        postRepository.incrementCommentCount(postId);

        return saved;
    }

    /**
     * 댓글을 생성하고 알림을 생성합니다.
     * 비즈니스 로직:
     * - 대댓글인 경우: 부모 댓글 작성자에게 REPLY 알림
     * - 일반 댓글인 경우: 게시글 작성자에게 COMMENT 알림
     * - 본인에게는 알림을 보내지 않음
     * - 탈퇴한 회원(recipientId가 NULL)에게는 알림을 보내지 않음
     *
     * @return CommentNotificationResult 댓글과 알림 정보
     */
    @Transactional
    public CommentNotificationResult createCommentWithNotification(Long postId, Long userId, Long parentId, String body, String imageUrl) {
        // 1. 댓글 생성
        Comment comment = createComment(postId, userId, parentId, body, imageUrl);

        // 2. 알림 수신자 및 타입 결정
        Long recipientId;
        NotificationType type;

        if (parentId != null) {
            // 대댓글인 경우: 부모 댓글 작성자에게 알림
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentNotFoundException(parentId));
            recipientId = parentComment.getUserId();
            type = NotificationType.REPLY;
        } else {
            // 일반 댓글인 경우: 게시글 작성자에게 알림
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException(postId));
            recipientId = post.getUserId();
            type = NotificationType.COMMENT;
        }

        // 3. 알림 생성 (탈퇴한 회원이나 본인에게는 알림을 보내지 않음)
        Notification notification = null;
        if (recipientId != null) {
            notification = notificationService.createNotification(
                    recipientId,
                    userId,
                    comment.getId(),
                    type
            );
        }

        return new CommentNotificationResult(comment, notification, recipientId, postId);
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    @Transactional
    public Comment updateComment(Long commentId, Long userId, boolean isAdmin, String body, String imageUrl) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!isAdmin && !comment.isAuthor(userId)) {
            throw new CommentAccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.update(body, imageUrl);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!isAdmin && !comment.isAuthor(userId)) {
            throw new CommentAccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        comment.delete();
        commentRepository.save(comment);
        
        // 게시글 댓글 수 감소
        postRepository.decrementCommentCount(comment.getPostId());
    }

    public long countByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    public java.util.Map<Long, Long> getCommentCounts(List<Long> postIds) {
        return commentRepository.countByPostIds(postIds);
    }
}
