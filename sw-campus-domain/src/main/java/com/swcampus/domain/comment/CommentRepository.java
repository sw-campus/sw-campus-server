package com.swcampus.domain.comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    List<Comment> findAllByIds(List<Long> ids);

    List<Comment> findByPostId(Long postId);

    long countByPostId(Long postId);

    java.util.Map<Long, Long> countByPostIds(java.util.List<Long> postIds);

    void incrementLikeCount(Long commentId);

    void decrementLikeCount(Long commentId);

    /**
     * 특정 유저의 댓글 작성자를 NULL로 설정 (회원 탈퇴 시 사용)
     */
    void setUserIdNullByUserId(Long userId);

    /**
     * 특정 사용자가 특정 게시글들에 단 가장 최근 댓글을 조회
     * @param userId 사용자 ID
     * @param postIds 게시글 ID 목록
     * @return Map<게시글ID, 댓글>
     */
    java.util.Map<Long, Comment> findLatestByUserIdAndPostIds(Long userId, java.util.List<Long> postIds);

    /**
     * 특정 댓글들의 대댓글 수를 조회
     * @param commentIds 댓글 ID 목록
     * @return Map<댓글ID, 대댓글수>
     */
    java.util.Map<Long, Long> countRepliesByParentIds(java.util.List<Long> commentIds);

    /**
     * 특정 게시글의 댓글을 일괄 soft delete (게시글 삭제 시 사용)
     */
    void softDeleteByPostId(Long postId);
}
