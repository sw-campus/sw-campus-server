package com.swcampus.infra.postgres.comment;

import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.comment.CommentRepository;
import com.swcampus.domain.comment.exception.CommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<Comment> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return jpaRepository.findAllById(ids).stream()
                .map(CommentEntity::toDomain)
                .toList();
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

    @Override
    public void setUserIdNullByUserId(Long userId) {
        jpaRepository.setUserIdNullByUserId(userId);
    }

    @Override
    public Map<Long, Comment> findLatestByUserIdAndPostIds(Long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<CommentEntity> comments = jpaRepository.findByUserIdAndPostIds(userId, postIds);

        // 각 postId별 가장 최근 댓글만 추출 (이미 createdAt DESC로 정렬되어 있음)
        return comments.stream()
                .collect(Collectors.toMap(
                        CommentEntity::getPostId,
                        CommentEntity::toDomain,
                        (existing, replacement) -> existing // 첫 번째 값(가장 최근)만 유지
                ));
    }

    @Override
    public Map<Long, Long> countRepliesByParentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return jpaRepository.countRepliesByParentIds(commentIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    public void softDeleteByPostId(Long postId) {
        jpaRepository.softDeleteByPostId(postId);
    }

    @Override
    public Page<Comment> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndNotDeleted(userId, pageable)
                .map(CommentEntity::toDomain);
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserIdAndNotDeleted(userId);
    }

    @Override
    public Map<Long, Long> countByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return jpaRepository.countByParentIds(parentIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
