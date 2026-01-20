package com.swcampus.api.comment.response;

import com.swcampus.domain.comment.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Schema(description = "댓글 응답")
public class CommentResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "부모 댓글 ID (대댓글인 경우)")
    private Long parentId;

    @Schema(description = "작성자 ID", example = "123")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String authorNickname;

    @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다!")
    private String body;

    @Schema(description = "첨부 이미지 URL")
    private String imageUrl;

    @Schema(description = "좋아요 수", example = "5")
    private Long likeCount;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "본인 작성 여부", example = "true")
    private boolean author;

    @Schema(description = "추천 여부", example = "false")
    private boolean liked;

    @Schema(description = "삭제 여부", example = "false")
    private boolean deleted;

    @Schema(description = "대댓글 목록")
    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();

    public static CommentResponse from(Comment comment, String authorNickname, boolean isAuthor, boolean isLiked) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .authorId(comment.getUserId())
                .authorNickname(comment.isDeleted() ? "" : authorNickname)
                .body(comment.isDeleted() ? "" : comment.getBody())
                .imageUrl(comment.isDeleted() ? null : comment.getImageUrl())
                .likeCount(comment.isDeleted() ? 0L : comment.getLikeCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .author(isAuthor)
                .liked(isLiked)
                .deleted(comment.isDeleted())
                .replies(new ArrayList<>())
                .build();
    }

    public void addReply(CommentResponse reply) {
        this.replies.add(reply);
    }
}
