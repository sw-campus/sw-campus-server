package com.swcampus.infra.postgres.comment;

import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.comment.CommentRepository;
import com.swcampus.domain.comment.exception.CommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentEntityRepository implements CommentRepository {

    private final CommentJpaRepository jpaRepository;

    @Override
    public Comment save(Comment comment) {
        CommentEntity entity;

        if (comment.getId() != null) {
            entity = jpaRepository.findById(comment.getId())
                    .orElseThrow(() -> new CommentNotFoundException(comment.getId()));
            entity.update(comment);
        } else {
            entity = CommentEntity.from(comment);
        }

        CommentEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return jpaRepository.findByIdAndNotDeleted(id)
                .map(CommentEntity::toDomain);
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        return jpaRepository.findAllByPostId(postId)
                .stream()
                .map(CommentEntity::toDomain)
                .toList();
    }

    @Override
    public long countByPostId(Long postId) {
        return jpaRepository.countByPostIdAndNotDeleted(postId);
    }

    @Override
    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return jpaRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    public void incrementLikeCount(Long commentId) {
        jpaRepository.incrementLikeCount(commentId);
    }

    @Override
    public void decrementLikeCount(Long commentId) {
        jpaRepository.decrementLikeCount(commentId);
    }
}
