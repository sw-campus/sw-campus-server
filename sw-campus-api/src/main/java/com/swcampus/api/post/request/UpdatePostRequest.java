package com.swcampus.api.post.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "게시글 수정 요청")
public class UpdatePostRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    @Schema(description = "게시글 제목", example = "수정된 제목입니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "게시글 본문 (HTML)", example = "<p>수정된 내용</p>")
    private String body;

    @Schema(description = "첨부 이미지 URL 목록")
    private List<String> images;

    @Schema(description = "태그 목록", example = "[\"java\", \"spring\"]")
    private List<String> tags;
}
