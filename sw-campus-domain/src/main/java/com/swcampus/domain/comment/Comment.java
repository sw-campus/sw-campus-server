package com.swcampus.domain.comment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String body;
    private String imageUrl;
    private Long likeCount;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Comment create(Long postId, Long userId, Long parentId, String body, String imageUrl) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.userId = userId;
        comment.parentId = parentId;
        comment.body = body;
        comment.imageUrl = imageUrl;
        comment.likeCount = 0L;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = LocalDateTime.now();
        return comment;
    }

    public static Comment of(Long id, Long postId, Long userId, Long parentId, String body,
                             String imageUrl, Long likeCount, boolean deleted,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        Comment comment = new Comment();
        comment.id = id;
        comment.postId = postId;
        comment.userId = userId;
        comment.parentId = parentId;
        comment.body = body;
        comment.imageUrl = imageUrl;
        comment.likeCount = likeCount;
        comment.deleted = deleted;
        comment.createdAt = createdAt;
        comment.updatedAt = updatedAt;
        return comment;
    }

    public void update(String body, String imageUrl) {
        this.body = body;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAuthor(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isReply() {
        return this.parentId != null;
    }
}
