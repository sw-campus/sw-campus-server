package com.swcampus.domain.comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    List<Comment> findAllByIds(List<Long> ids);

    List<Comment> findByPostId(Long postId);

    long countByPostId(Long postId);

    java.util.Map<Long, Long> countByPostIds(java.util.List<Long> postIds);

    void incrementLikeCount(Long commentId);

    void decrementLikeCount(Long commentId);

    /**
     * 특정 유저의 댓글 작성자를 NULL로 설정 (회원 탈퇴 시 사용)
     */
    void setUserIdNullByUserId(Long userId);
}
