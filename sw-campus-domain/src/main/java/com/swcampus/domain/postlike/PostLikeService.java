package com.swcampus.domain.postlike;

import com.swcampus.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    /**
     * 게시글 추천 토글 - 이미 추천했으면 취소, 없으면 추천
     * @return true: 추천 추가됨, false: 추천 취소됨
     */
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            postLikeRepository.deleteByUserIdAndPostId(userId, postId);
            postRepository.decrementLikeCount(postId);
            return false;
        } else {
            PostLike postLike = PostLike.create(userId, postId);
            postLikeRepository.save(postLike);
            postRepository.incrementLikeCount(postId);
            return true;
        }
    }

    /**
     * 특정 게시글 추천 여부 확인
     */
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null) {
            return false;
        }
        return postLikeRepository.existsByUserIdAndPostId(userId, postId);
    }

    /**
     * 게시글 추천 수 조회
     */
    public long getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    /**
     * 사용자가 추천한 게시글 ID 목록 조회 (일괄 조회용)
     */
    public Set<Long> getLikedPostIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return postLikeRepository.findPostIdsByUserId(userId);
    }

    /**
     * 게시글에 좋아요 누른 사용자 ID 목록 조회
     */
    public List<Long> getLikerIds(Long postId) {
        return postLikeRepository.findUserIdsByPostId(postId);
    }
}
