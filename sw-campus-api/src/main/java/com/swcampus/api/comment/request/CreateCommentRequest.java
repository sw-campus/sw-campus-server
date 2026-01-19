package com.swcampus.api.comment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 생성 요청")
public class CreateCommentRequest {

    @NotNull(message = "게시글 ID는 필수입니다.")
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "null")
    private Long parentId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 1000자 이내여야 합니다.")
    @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다!")
    private String body;

    @Schema(description = "첨부 이미지 URL")
    private String imageUrl;
}
