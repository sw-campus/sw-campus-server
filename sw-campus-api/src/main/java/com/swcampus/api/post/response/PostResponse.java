package com.swcampus.api.post.response;

import com.swcampus.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 목록 응답")
public class PostResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 제목", example = "Spring Boot 질문입니다")
    private String title;

    @Schema(description = "작성자 ID", example = "123")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String authorNickname;

    @Schema(description = "카테고리 ID", example = "3")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "자유게시판")
    private String categoryName;

    @Schema(description = "태그 목록")
    private List<String> tags;

    @Schema(description = "조회수", example = "100")
    private Long viewCount;

    @Schema(description = "좋아요 수", example = "10")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "5")
    private Long commentCount;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "이미지 포함 여부", example = "true")
    private boolean hasImage;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/image.jpg")
    private String thumbnailUrl;

    @Schema(description = "고정 게시글 여부", example = "false")
    private boolean pinned;

    public static PostResponse from(Post post, String authorNickname, String categoryName, Long commentCount) {
        List<String> images = post.getImages();
        boolean hasImage = images != null && !images.isEmpty();
        String thumbnail = hasImage ? images.get(0) : null;
        
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorId(post.getUserId())
                .authorNickname(authorNickname)
                .categoryId(post.getBoardCategoryId())
                .categoryName(categoryName)
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(commentCount != null ? commentCount : 0L)
                .createdAt(post.getCreatedAt())
                .hasImage(hasImage)
                .thumbnailUrl(thumbnail)
                .pinned(post.isPinned())
                .build();
    }
}
