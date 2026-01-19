package com.swcampus.domain.postlike;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;

    public static PostLike create(Long userId, Long postId) {
        PostLike postLike = new PostLike();
        postLike.userId = userId;
        postLike.postId = postId;
        postLike.createdAt = LocalDateTime.now();
        return postLike;
    }

    public static PostLike of(Long id, Long userId, Long postId, LocalDateTime createdAt) {
        PostLike postLike = new PostLike();
        postLike.id = id;
        postLike.userId = userId;
        postLike.postId = postId;
        postLike.createdAt = createdAt;
        return postLike;
    }
}
