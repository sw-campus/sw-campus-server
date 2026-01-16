package com.swcampus.infra.postgres.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT c FROM CommentEntity c WHERE c.id = :id AND c.deleted = false")
    Optional<CommentEntity> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT c FROM CommentEntity c WHERE c.postId = :postId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<CommentEntity> findByPostIdAndNotDeleted(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.postId = :postId AND c.deleted = false")
    long countByPostIdAndNotDeleted(@Param("postId") Long postId);

    @Query("SELECT c.postId, COUNT(c) FROM CommentEntity c WHERE c.postId IN :postIds AND c.deleted = false GROUP BY c.postId")
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);
}
