package com.swcampus.api.user.response;

import com.swcampus.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "댓글 단 게시글 응답 (내 댓글 포함)")
public class CommentedPostResponse {

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

    @Schema(description = "내가 단 댓글 내용", example = "좋은 글이네요!")
    private String myComment;

    @Schema(description = "내가 단 댓글 작성일시")
    private LocalDateTime myCommentCreatedAt;

    @Schema(description = "내 댓글 좋아요 수", example = "5")
    private Long myCommentLikeCount;

    @Schema(description = "내 댓글 대댓글 수", example = "2")
    private Long myCommentReplyCount;

    public static CommentedPostResponse from(Post post, String authorNickname, String categoryName,
                                              Long commentCount, String myComment, LocalDateTime myCommentCreatedAt,
                                              Long myCommentLikeCount, Long myCommentReplyCount) {
        List<String> images = post.getImages();
        boolean hasImage = images != null && !images.isEmpty();
        String thumbnail = hasImage ? images.get(0) : null;

        return CommentedPostResponse.builder()
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
                .myComment(myComment)
                .myCommentCreatedAt(myCommentCreatedAt)
                .myCommentLikeCount(myCommentLikeCount != null ? myCommentLikeCount : 0L)
                .myCommentReplyCount(myCommentReplyCount != null ? myCommentReplyCount : 0L)
                .build();
    }
}
