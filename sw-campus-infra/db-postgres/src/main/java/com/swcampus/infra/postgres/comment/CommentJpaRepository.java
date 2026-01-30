package com.swcampus.infra.postgres.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT c FROM CommentEntity c WHERE c.id = :id AND c.deleted = false")
    Optional<CommentEntity> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM CommentEntity c WHERE c.postId = :postId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<CommentEntity> findByPostIdAndNotDeleted(@Param("postId") Long postId);

    @Query("SELECT DISTINCT c FROM CommentEntity c WHERE c.postId = :postId ORDER BY c.createdAt ASC")
    List<CommentEntity> findAllByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.postId = :postId AND c.deleted = false")
    long countByPostIdAndNotDeleted(@Param("postId") Long postId);

    @Query("SELECT c.postId, COUNT(c) FROM CommentEntity c WHERE c.postId IN :postIds AND c.deleted = false GROUP BY c.postId")
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE CommentEntity c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE CommentEntity c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    void decrementLikeCount(@Param("commentId") Long commentId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE CommentEntity c SET c.userId = NULL WHERE c.userId = :userId")
    void setUserIdNullByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM CommentEntity c WHERE c.userId = :userId AND c.postId IN :postIds AND c.deleted = false ORDER BY c.createdAt DESC")
    List<CommentEntity> findByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    @Query("SELECT c.parentId, COUNT(c) FROM CommentEntity c WHERE c.parentId IN :parentIds AND c.deleted = false GROUP BY c.parentId")
    List<Object[]> countRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE CommentEntity c SET c.deleted = true WHERE c.postId = :postId AND c.deleted = false")
    void softDeleteByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM CommentEntity c WHERE c.userId = :userId AND c.deleted = false")
    Page<CommentEntity> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.userId = :userId AND c.deleted = false")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT c.parentId, COUNT(c) FROM CommentEntity c WHERE c.parentId IN :parentIds AND c.deleted = false GROUP BY c.parentId")
    List<Object[]> countByParentIds(@Param("parentIds") List<Long> parentIds);
}
