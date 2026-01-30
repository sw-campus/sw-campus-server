package com.swcampus.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PostRepository {
    
    Post save(Post post);
    
    Optional<Post> findById(Long id);
    
    Page<Post> findAll(List<Long> categoryIds, List<String> tags, Pageable pageable);
    
    void incrementViewCount(Long id);

    void incrementCommentCount(Long id);

    void decrementCommentCount(Long id);

    void incrementLikeCount(Long id);

    void decrementLikeCount(Long id);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);

    /**
     * 게시글 목록을 작성자 닉네임, 카테고리 이름과 함께 조회합니다.
     * N+1 문제를 해결하기 위해 JOIN 쿼리를 사용합니다.
     * @param keyword 검색어 (제목, 본문, 태그에서 검색)
     */
    Page<PostSummary> findAllWithDetails(List<Long> categoryIds, List<String> tags, String keyword, Pageable pageable);

    /**
     * 이전 게시글 조회 (현재 게시글보다 id가 작고, 삭제되지 않은 게시글 중 가장 큰 id)
     */
    Optional<PostSummary> findPreviousPost(Long currentPostId);

    /**
     * 다음 게시글 조회 (현재 게시글보다 id가 크고, 삭제되지 않은 게시글 중 가장 작은 id)
     */
    Optional<PostSummary> findNextPost(Long currentPostId);

    /**
     * 특정 유저가 작성한 게시글 목록 조회 (삭제되지 않은 게시글만)
     */
    Page<PostSummary> findByUserId(Long userId, Pageable pageable);

    /**
     * 특정 유저가 작성한 게시글 수 조회 (삭제되지 않은 게시글만)
     */
    long countByUserId(Long userId);

    /**
     * 여러 게시글 ID로 게시글 목록 조회 (삭제되지 않은 게시글만)
     * 북마크 목록 조회 등에 사용
     */
    List<PostSummary> findAllByIds(List<Long> ids);

    /**
     * 특정 유저가 댓글을 단 게시글 목록 조회 (삭제되지 않은 게시글만)
     */
    Page<PostSummary> findCommentedByUserId(Long userId, Pageable pageable);

    /**
     * 특정 유저의 게시글 작성자를 NULL로 설정 (회원 탈퇴 시 사용)
     */
    void setUserIdNullByUserId(Long userId);

    /**
     * 여러 게시글 ID로 게시글 제목을 일괄 조회합니다.
     */
    Map<Long, String> findTitlesByIds(List<Long> ids);
}
