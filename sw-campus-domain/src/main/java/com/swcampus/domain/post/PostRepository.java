package com.swcampus.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    
    Post save(Post post);
    
    Optional<Post> findById(Long id);
    
    Page<Post> findAll(Long categoryId, List<String> tags, Pageable pageable);
    
    void incrementViewCount(Long id);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);

    /**
     * 게시글 목록을 작성자 닉네임, 카테고리 이름과 함께 조회합니다.
     * N+1 문제를 해결하기 위해 JOIN 쿼리를 사용합니다.
     */
    Page<PostSummary> findAllWithDetails(Long categoryId, List<String> tags, Pageable pageable);
}
