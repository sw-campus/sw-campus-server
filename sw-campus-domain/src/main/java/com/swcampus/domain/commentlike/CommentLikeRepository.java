package com.swcampus.domain.commentlike;

import java.util.Set;

public interface CommentLikeRepository {

    CommentLike save(CommentLike commentLike);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    Set<Long> findCommentIdsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
