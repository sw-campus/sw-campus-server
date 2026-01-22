package com.swcampus.infra.postgres.postlike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostLikeJpaRepository extends JpaRepository<PostLikeEntity, Long> {

    void deleteByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    @Query("SELECT pl.postId FROM PostLikeEntity pl WHERE pl.userId = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT pl.userId FROM PostLikeEntity pl WHERE pl.postId = :postId ORDER BY pl.createdAt DESC")
    List<Long> findUserIdsByPostId(@Param("postId") Long postId);
}
