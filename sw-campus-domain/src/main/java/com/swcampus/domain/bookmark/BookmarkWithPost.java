package com.swcampus.domain.bookmark;

import com.swcampus.domain.post.PostSummary;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 북마크와 게시글 정보를 함께 담는 DTO
 */
@Getter
public class BookmarkWithPost {
    private final Long bookmarkId;
    private final Long postId;
    private final LocalDateTime bookmarkedAt;
    private final PostSummary postSummary;

    private BookmarkWithPost(Bookmark bookmark, PostSummary postSummary) {
        this.bookmarkId = bookmark.getId();
        this.postId = bookmark.getPostId();
        this.bookmarkedAt = bookmark.getCreatedAt();
        this.postSummary = postSummary;
    }

    public static BookmarkWithPost of(Bookmark bookmark, PostSummary postSummary) {
        return new BookmarkWithPost(bookmark, postSummary);
    }
}
