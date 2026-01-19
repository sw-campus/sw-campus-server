package com.swcampus.domain.commentlike;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final com.swcampus.domain.comment.CommentRepository commentRepository;

    /**
     * 댓글 추천 토글 - 이미 추천했으면 취소, 없으면 추천
     * @return true: 추천 추가됨, false: 추천 취소됨
     */
    @Transactional
    public boolean toggleLike(Long userId, Long commentId) {
        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
            commentRepository.decrementLikeCount(commentId);
            return false;
        } else {
            CommentLike commentLike = CommentLike.create(userId, commentId);
            commentLikeRepository.save(commentLike);
            commentRepository.incrementLikeCount(commentId);
            return true;
        }
    }

    /**
     * 특정 댓글 추천 여부 확인
     */
    public boolean isLiked(Long userId, Long commentId) {
        if (userId == null) {
            return false;
        }
        return commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
    }

    /**
     * 댓글 추천 수 조회
     */
    public long getLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    /**
     * 사용자가 추천한 댓글 ID 목록 조회 (일괄 조회용)
     */
    public Set<Long> getLikedCommentIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return commentLikeRepository.findCommentIdsByUserId(userId);
    }
}
