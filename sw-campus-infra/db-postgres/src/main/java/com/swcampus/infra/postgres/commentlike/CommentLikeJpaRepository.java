package com.swcampus.infra.postgres.commentlike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeJpaRepository extends JpaRepository<CommentLikeEntity, Long> {

    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    Optional<CommentLikeEntity> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    @Query("SELECT cl.commentId FROM CommentLikeEntity cl WHERE cl.userId = :userId")
    List<Long> findCommentIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
