package com.swcampus.api.bookmark.response;

import com.swcampus.domain.bookmark.Bookmark;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponse {
    private Long id;
    private Long postId;
    private LocalDateTime createdAt;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .postId(bookmark.getPostId())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
