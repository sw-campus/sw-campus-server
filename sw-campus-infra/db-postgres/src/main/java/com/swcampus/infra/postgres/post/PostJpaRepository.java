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

    /**
     * 게시글 목록을 작성자 닉네임, 카테고리 이름과 함께 조회합니다.
     * N+1 문제를 해결하기 위해 JOIN 쿼리를 사용합니다.
     */
    @Query(value = """
            SELECT 
                p.post_id,
                p.board_category_id,
                p.user_id,
                p.post_title,
                p.post_body,
                p.post_images,
                p.tags,
                p.view_count,
                p.like_count,
                p.selected_comment_id,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                COALESCE(m.nickname, '알 수 없음') as author_nickname,
                COALESCE(bc.board_category_name, '알 수 없음') as category_name
            FROM swcampus.posts p
            LEFT JOIN swcampus.members m ON p.user_id = m.user_id
            LEFT JOIN swcampus.board_categories bc ON p.board_category_id = bc.board_category_id
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
    Page<Object[]> findAllWithDetails(
            @Param("categoryId") Long categoryId,
            @Param("tags") String[] tags,
            Pageable pageable);
}
