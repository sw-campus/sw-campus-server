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
            AND (CAST(:categoryIds AS bigint[]) IS NULL OR p.board_category_id = ANY(CAST(:categoryIds AS bigint[])))
            AND (CAST(:tags AS text[]) IS NULL OR p.tags && CAST(:tags AS text[]))
            """,
            countQuery = """
            SELECT COUNT(*) FROM swcampus.posts p
            WHERE p.is_deleted = false
            AND (CAST(:categoryIds AS bigint[]) IS NULL OR p.board_category_id = ANY(CAST(:categoryIds AS bigint[])))
            AND (CAST(:tags AS text[]) IS NULL OR p.tags && CAST(:tags AS text[]))
            """,
            nativeQuery = true)
    Page<PostEntity> findAllWithFilters(
            @Param("categoryIds") Long[] categoryIds,
            @Param("tags") String[] tags,
            Pageable pageable);

    @Modifying
    @Query("UPDATE PostEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PostEntity p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PostEntity p SET p.commentCount = CASE WHEN p.commentCount > 0 THEN p.commentCount - 1 ELSE 0 END WHERE p.id = :id")
    void decrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PostEntity p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PostEntity p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :id")
    void decrementLikeCount(@Param("id") Long id);

    /**
     * 게시글 목록을 작성자 닉네임, 카테고리 이름과 함께 조회합니다.
     * N+1 문제를 해결하기 위해 JOIN 쿼리를 사용합니다.
     * @param keyword 검색어 (제목, 본문, 태그에서 검색)
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
                p.comment_count,
                p.selected_comment_id,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                p.is_pinned,
                COALESCE(m.nickname, '알 수 없음') as author_nickname,
                COALESCE(bc.board_category_name, '알 수 없음') as category_name
            FROM swcampus.posts p
            LEFT JOIN swcampus.members m ON p.user_id = m.user_id
            LEFT JOIN swcampus.board_categories bc ON p.board_category_id = bc.board_category_id
            WHERE p.is_deleted = false
            AND (CAST(:categoryIds AS bigint[]) IS NULL OR p.board_category_id = ANY(CAST(:categoryIds AS bigint[])))
            AND (CAST(:tags AS text[]) IS NULL OR p.tags && CAST(:tags AS text[]))
            AND (:keyword IS NULL OR :keyword = '' OR 
                 p.post_title ILIKE '%' || :keyword || '%' OR 
                 p.post_body ILIKE '%' || :keyword || '%' OR 
                 :keyword = ANY(p.tags))
            """,
            countQuery = """
            SELECT COUNT(*) FROM swcampus.posts p
            WHERE p.is_deleted = false
            AND (CAST(:categoryIds AS bigint[]) IS NULL OR p.board_category_id = ANY(CAST(:categoryIds AS bigint[])))
            AND (CAST(:tags AS text[]) IS NULL OR p.tags && CAST(:tags AS text[]))
            AND (:keyword IS NULL OR :keyword = '' OR 
                 p.post_title ILIKE '%' || :keyword || '%' OR 
                 p.post_body ILIKE '%' || :keyword || '%' OR 
                 :keyword = ANY(p.tags))
            """,
            nativeQuery = true)
    Page<Object[]> findAllWithDetails(
            @Param("categoryIds") Long[] categoryIds,
            @Param("tags") String[] tags,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 이전 게시글 조회 (현재 게시글보다 id가 작고, 삭제되지 않은 게시글 중 가장 큰 id)
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
                p.comment_count,
                p.selected_comment_id,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                p.is_pinned,
                COALESCE(m.nickname, '알 수 없음') as author_nickname,
                COALESCE(bc.board_category_name, '알 수 없음') as category_name
            FROM swcampus.posts p
            LEFT JOIN swcampus.members m ON p.user_id = m.user_id
            LEFT JOIN swcampus.board_categories bc ON p.board_category_id = bc.board_category_id
            WHERE p.is_deleted = false AND p.post_id < :currentPostId
            ORDER BY p.post_id DESC
            LIMIT 1
            """, nativeQuery = true)
    java.util.List<Object[]> findPreviousPostWithDetails(@Param("currentPostId") Long currentPostId);

    /**
     * 다음 게시글 조회 (현재 게시글보다 id가 크고, 삭제되지 않은 게시글 중 가장 작은 id)
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
                p.comment_count,
                p.selected_comment_id,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                p.is_pinned,
                COALESCE(m.nickname, '알 수 없음') as author_nickname,
                COALESCE(bc.board_category_name, '알 수 없음') as category_name
            FROM swcampus.posts p
            LEFT JOIN swcampus.members m ON p.user_id = m.user_id
            LEFT JOIN swcampus.board_categories bc ON p.board_category_id = bc.board_category_id
            WHERE p.is_deleted = false AND p.post_id > :currentPostId
            ORDER BY p.post_id ASC
            LIMIT 1
            """, nativeQuery = true)
    java.util.List<Object[]> findNextPostWithDetails(@Param("currentPostId") Long currentPostId);

    /**
     * 특정 유저가 작성한 게시글 목록 조회 (삭제되지 않은 게시글만)
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
                p.comment_count,
                p.selected_comment_id,
                p.is_deleted,
                p.created_at,
                p.updated_at,
                p.is_pinned,
                COALESCE(m.nickname, '알 수 없음') as author_nickname,
                COALESCE(bc.board_category_name, '알 수 없음') as category_name
            FROM swcampus.posts p
            LEFT JOIN swcampus.members m ON p.user_id = m.user_id
            LEFT JOIN swcampus.board_categories bc ON p.board_category_id = bc.board_category_id
            WHERE p.is_deleted = false AND p.user_id = :userId
            """,
            countQuery = """
            SELECT COUNT(*) FROM swcampus.posts p
            WHERE p.is_deleted = false AND p.user_id = :userId
            """,
            nativeQuery = true)
    Page<Object[]> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 유저가 작성한 게시글 수 조회 (삭제되지 않은 게시글만)
     */
    @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.userId = :userId AND p.deleted = false")
    long countByUserIdNotDeleted(@Param("userId") Long userId);
}
