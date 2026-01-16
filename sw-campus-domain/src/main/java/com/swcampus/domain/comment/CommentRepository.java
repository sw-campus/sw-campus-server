package com.swcampus.domain.comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    List<Comment> findByPostId(Long postId);

    long countByPostId(Long postId);

    java.util.Map<Long, Long> countByPostIds(java.util.List<Long> postIds);
}
