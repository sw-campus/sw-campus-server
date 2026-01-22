package com.swcampus.domain.postlike;

import java.util.List;
import java.util.Set;

public interface PostLikeRepository {

    PostLike save(PostLike postLike);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    Set<Long> findPostIdsByUserId(Long userId);

    List<Long> findUserIdsByPostId(Long postId);
}
