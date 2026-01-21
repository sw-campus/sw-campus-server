package com.swcampus.domain.post;

import lombok.Builder;
import lombok.Getter;

/**
 * 게시글 목록 조회용 DTO
 * Post와 함께 작성자 닉네임, 카테고리 이름을 포함하여 N+1 문제를 해결합니다.
 */
@Getter
@Builder
public class PostSummary {
    private final Post post;
    private final String authorNickname;
    private final String categoryName;
}
