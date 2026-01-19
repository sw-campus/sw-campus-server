package com.swcampus.domain.commentlike;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {
    private Long id;
    private Long userId;
    private Long commentId;
    private LocalDateTime createdAt;

    public static CommentLike create(Long userId, Long commentId) {
        CommentLike commentLike = new CommentLike();
        commentLike.userId = userId;
        commentLike.commentId = commentId;
        commentLike.createdAt = LocalDateTime.now();
        return commentLike;
    }

    public static CommentLike of(Long id, Long userId, Long commentId, LocalDateTime createdAt) {
        CommentLike commentLike = new CommentLike();
        commentLike.id = id;
        commentLike.userId = userId;
        commentLike.commentId = commentId;
        commentLike.createdAt = createdAt;
        return commentLike;
    }
}
