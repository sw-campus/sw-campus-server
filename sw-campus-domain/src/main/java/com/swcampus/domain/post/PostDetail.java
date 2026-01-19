package com.swcampus.domain.post;

import lombok.Builder;
import lombok.Getter;

/**
 * 게시글 상세 조회용 DTO
 * Post와 함께 작성자 닉네임, 카테고리 이름, 댓글 수를 포함합니다.
 * 탈퇴한 회원의 경우 authorNickname이 "알 수 없음"으로 설정됩니다.
 */
@Getter
@Builder
public class PostDetail {
    private final Post post;
    private final String authorNickname;
    private final String categoryName;
    private final long commentCount;
}
