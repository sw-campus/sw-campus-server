package com.swcampus.domain.post;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    private Long id;
    private Long boardCategoryId;
    private Long userId;
    private String title;
    private String body;
    private List<String> images = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private Long viewCount;
    private Long likeCount;
    private Long selectedCommentId;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Post create(Long boardCategoryId, Long userId, String title, String body,
                              List<String> images, List<String> tags) {
        Post post = new Post();
        post.boardCategoryId = boardCategoryId;
        post.userId = userId;
        post.title = title;
        post.body = body;
        post.images = images != null ? images : new ArrayList<>();
        post.tags = tags != null ? tags : new ArrayList<>();
        post.viewCount = 0L;
        post.likeCount = 0L;
        post.deleted = false;
        post.createdAt = LocalDateTime.now();
        post.updatedAt = LocalDateTime.now();
        return post;
    }

    public static Post of(Long id, Long boardCategoryId, Long userId, String title, String body,
                          List<String> images, List<String> tags, Long viewCount, Long likeCount,
                          Long selectedCommentId, boolean deleted,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        Post post = new Post();
        post.id = id;
        post.boardCategoryId = boardCategoryId;
        post.userId = userId;
        post.title = title;
        post.body = body;
        post.images = images != null ? images : new ArrayList<>();
        post.tags = tags != null ? tags : new ArrayList<>();
        post.viewCount = viewCount;
        post.likeCount = likeCount;
        post.selectedCommentId = selectedCommentId;
        post.deleted = deleted;
        post.createdAt = createdAt;
        post.updatedAt = updatedAt;
        return post;
    }

    public void update(String title, String body, List<String> images, List<String> tags) {
        this.title = title;
        this.body = body;
        this.images = images != null ? images : new ArrayList<>();
        this.tags = tags != null ? tags : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    public void selectComment(Long commentId) {
        this.selectedCommentId = commentId;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAuthor(Long userId) {
        return this.userId.equals(userId);
    }
}
