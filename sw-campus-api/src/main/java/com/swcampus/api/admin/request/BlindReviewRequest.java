package com.swcampus.api.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "후기 블라인드 처리 요청")
public record BlindReviewRequest(
    @Schema(description = "블라인드 여부 (true: 비공개, false: 공개)", example = "true")
    @NotNull(message = "blurred 값은 필수입니다")
    Boolean blurred
) {}
