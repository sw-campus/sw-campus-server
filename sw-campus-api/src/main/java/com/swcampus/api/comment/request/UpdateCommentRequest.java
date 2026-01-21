package com.swcampus.api.comment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 수정 요청")
public class UpdateCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 1000자 이내여야 합니다.")
    @Schema(description = "수정할 댓글 내용", example = "수정된 내용입니다.")
    private String body;

    @Schema(description = "첨부 이미지 URL")
    private String imageUrl;
}
