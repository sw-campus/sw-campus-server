package com.swcampus.api.post.response;

import com.swcampus.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 상세 응답")
public class PostDetailResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 제목", example = "Spring Boot 질문입니다")
    private String title;

    @Schema(description = "게시글 본문 (HTML)")
    private String body;

    @Schema(description = "작성자 ID", example = "123")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String authorNickname;

    @Schema(description = "카테고리 ID", example = "3")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "자유게시판")
    private String categoryName;

    @Schema(description = "첨부 이미지 URL 목록")
    private List<String> images;

    @Schema(description = "태그 목록")
    private List<String> tags;

    @Schema(description = "조회수", example = "100")
    private Long viewCount;

    @Schema(description = "좋아요 수", example = "10")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "5")
    private Long commentCount;

    @Schema(description = "채택된 댓글 ID")
    private Long selectedCommentId;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "북마크 여부", example = "false")
    private boolean bookmarked;

    @Schema(description = "좋아요 여부", example = "false")
    private boolean liked;

    @Schema(description = "본인 작성 여부", example = "true")
    private boolean isAuthor;

    public static PostDetailResponse from(Post post, String authorNickname, String categoryName,
                                          Long commentCount, boolean bookmarked, boolean liked, boolean isAuthor) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .authorId(post.getUserId())
                .authorNickname(authorNickname)
                .categoryId(post.getBoardCategoryId())
                .categoryName(categoryName)
                .images(post.getImages())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(commentCount != null ? commentCount : 0L)
                .selectedCommentId(post.getSelectedCommentId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .bookmarked(bookmarked)
                .liked(liked)
                .isAuthor(isAuthor)
                .build();
    }
}
