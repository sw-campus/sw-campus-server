package com.swcampus.api.bookmark.response;

import com.swcampus.domain.bookmark.BookmarkWithPost;
import com.swcampus.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "북마크 게시글 응답")
public class BookmarkWithPostResponse {

    @Schema(description = "북마크 ID", example = "1")
    private Long bookmarkId;

    @Schema(description = "게시글 ID", example = "10")
    private Long postId;

    @Schema(description = "게시글 제목", example = "Spring Boot 질문입니다")
    private String title;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String authorNickname;

    @Schema(description = "카테고리 이름", example = "자유게시판")
    private String categoryName;

    @Schema(description = "썸네일 이미지 URL")
    private String thumbnailUrl;

    @Schema(description = "댓글 수", example = "5")
    private Long commentCount;

    @Schema(description = "좋아요 수", example = "10")
    private Long likeCount;

    @Schema(description = "조회 수", example = "42")
    private Long viewCount;

    @Schema(description = "게시글 작성일시")
    private LocalDateTime postCreatedAt;

    @Schema(description = "북마크 등록일시")
    private LocalDateTime bookmarkedAt;

    public static BookmarkWithPostResponse from(BookmarkWithPost bookmarkWithPost) {
        Post post = bookmarkWithPost.getPostSummary().getPost();
        List<String> images = post.getImages();
        String thumbnail = (images != null && !images.isEmpty()) ? images.get(0) : null;

        return BookmarkWithPostResponse.builder()
                .bookmarkId(bookmarkWithPost.getBookmarkId())
                .postId(bookmarkWithPost.getPostId())
                .title(post.getTitle())
                .authorNickname(bookmarkWithPost.getPostSummary().getAuthorNickname())
                .categoryName(bookmarkWithPost.getPostSummary().getCategoryName())
                .thumbnailUrl(thumbnail)
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .postCreatedAt(post.getCreatedAt())
                .bookmarkedAt(bookmarkWithPost.getBookmarkedAt())
                .build();
    }
}
