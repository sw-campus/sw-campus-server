package com.swcampus.infra.postgres.postlike;

import com.swcampus.domain.postlike.PostLike;
import com.swcampus.domain.postlike.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PostLikeEntityRepository implements PostLikeRepository {

    private final PostLikeJpaRepository jpaRepository;

    @Override
    public PostLike save(PostLike postLike) {
        PostLikeEntity entity = PostLikeEntity.from(postLike);
        PostLikeEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndPostId(Long userId, Long postId) {
        jpaRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public boolean existsByUserIdAndPostId(Long userId, Long postId) {
        return jpaRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public long countByPostId(Long postId) {
        return jpaRepository.countByPostId(postId);
    }

    @Override
    public Set<Long> findPostIdsByUserId(Long userId) {
        return new HashSet<>(jpaRepository.findPostIdsByUserId(userId));
    }

    @Override
    public List<Long> findUserIdsByPostId(Long postId) {
        return jpaRepository.findUserIdsByPostId(postId);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
