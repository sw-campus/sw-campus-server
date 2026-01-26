package com.swcampus.infra.postgres.commentlike;

import com.swcampus.domain.commentlike.CommentLike;
import com.swcampus.domain.commentlike.CommentLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CommentLikeEntityRepository implements CommentLikeRepository {

    private final CommentLikeJpaRepository jpaRepository;

    @Override
    public CommentLike save(CommentLike commentLike) {
        CommentLikeEntity entity = CommentLikeEntity.from(commentLike);
        CommentLikeEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndCommentId(Long userId, Long commentId) {
        jpaRepository.deleteByUserIdAndCommentId(userId, commentId);
    }

    @Override
    public boolean existsByUserIdAndCommentId(Long userId, Long commentId) {
        return jpaRepository.existsByUserIdAndCommentId(userId, commentId);
    }

    @Override
    public long countByCommentId(Long commentId) {
        return jpaRepository.countByCommentId(commentId);
    }

    @Override
    public Set<Long> findCommentIdsByUserId(Long userId) {
        return new HashSet<>(jpaRepository.findCommentIdsByUserId(userId));
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
