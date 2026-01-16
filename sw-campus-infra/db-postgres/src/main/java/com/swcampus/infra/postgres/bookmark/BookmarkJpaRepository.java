package com.swcampus.infra.postgres.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkJpaRepository extends JpaRepository<BookmarkEntity, Long> {

    Optional<BookmarkEntity> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    List<BookmarkEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT b.postId FROM BookmarkEntity b WHERE b.userId = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}
