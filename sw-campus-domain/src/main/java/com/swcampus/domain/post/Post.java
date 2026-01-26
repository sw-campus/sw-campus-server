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
    private Long commentCount;
    private Long selectedCommentId;
    private boolean pinned;
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
        post.commentCount = 0L;
        post.deleted = false;
        post.createdAt = LocalDateTime.now();
        post.updatedAt = LocalDateTime.now();
        return post;
    }

    public static Post of(Long id, Long boardCategoryId, Long userId, String title, String body,
                          List<String> images, List<String> tags, Long viewCount, Long likeCount,
                          Long commentCount, Long selectedCommentId, boolean pinned, boolean deleted,
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
        post.commentCount = commentCount;
        post.selectedCommentId = selectedCommentId;
        post.pinned = pinned;
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

    public void incrementCommentCount() {
        this.commentCount = this.commentCount + 1;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount = this.commentCount - 1;
        }
    }

    public void selectComment(Long commentId) {
        this.selectedCommentId = commentId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 작성자 여부 확인
     * 탈퇴한 회원의 게시글(this.userId가 NULL)은 누구도 작성자가 아님
     */
    public boolean isAuthor(Long userId) {
        if (this.userId == null) {
            return false;
        }
        return this.userId.equals(userId);
    }

    public void togglePin() {
        this.pinned = !this.pinned;
        this.updatedAt = LocalDateTime.now();
    }
}
