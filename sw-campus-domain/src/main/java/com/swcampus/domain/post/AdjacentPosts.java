package com.swcampus.domain.post;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이전/다음 게시글 정보를 담는 DTO
 */
@Getter
@AllArgsConstructor
public class AdjacentPosts {
    private final PostSummary previous;
    private final PostSummary next;
    
    public boolean hasPrevious() {
        return previous != null;
    }
    
    public boolean hasNext() {
        return next != null;
    }
}
