package com.swcampus.infra.postgres.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<PostEntity, Long> {

    @Query("SELECT p FROM PostEntity p WHERE p.id = :id AND p.deleted = false")
    Optional<PostEntity> findByIdAndNotDeleted(@Param("id") Long id);

    @Query(value = """
            SELECT * FROM swcampus.posts p
            WHERE p.is_deleted = false
            AND (:categoryId IS NULL OR p.board_category_id = :categoryId)
            AND (CAST(:tags AS text[]) IS NULL OR p.tags && CAST(:tags AS text[]))
            """,
            countQuery = """
            SELECT COUNT(*) FROM swcampus.posts p
            WHERE p.is_deleted = false
            AND (:categoryId IS NULL OR p.board_category_id = :categoryId)
            AND (CAST(:tags AS text[]) IS NULL OR p.tags && CAST(:tags AS text[]))
            """,
            nativeQuery = true)
    Page<PostEntity> findAllWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("tags") String[] tags,
            Pageable pageable);

    @Modifying
    @Query("UPDATE PostEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.id = :postId AND p.deleted = false")
    long countByPostId(@Param("postId") Long postId);
}
