package com.swcampus.domain.bookmark;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;

    public static Bookmark create(Long userId, Long postId) {
        Bookmark bookmark = new Bookmark();
        bookmark.userId = userId;
        bookmark.postId = postId;
        bookmark.createdAt = LocalDateTime.now();
        return bookmark;
    }

    public static Bookmark of(Long id, Long userId, Long postId, LocalDateTime createdAt) {
        Bookmark bookmark = new Bookmark();
        bookmark.id = id;
        bookmark.userId = userId;
        bookmark.postId = postId;
        bookmark.createdAt = createdAt;
        return bookmark;
    }
}
