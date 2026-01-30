package com.swcampus.api.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "유저 댓글 응답")
public class UserCommentResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "게시글 ID", example = "10")
    private Long postId;

    @Schema(description = "게시글 제목", example = "Spring Boot 질문입니다")
    private String postTitle;

    @Schema(description = "댓글 내용", example = "좋은 글이네요!")
    private String body;

    @Schema(description = "좋아요 수", example = "5")
    private Long likeCount;

    @Schema(description = "대댓글 수", example = "2")
    private Long replyCount;

    @Schema(description = "대댓글 여부", example = "false")
    private boolean reply;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;
}
