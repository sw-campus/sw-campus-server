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

    long countByPostId(Long postId);
}
