package com.swcampus.api.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "순서 변경 요청")
public record ReorderRequest(
        @Schema(description = "새로운 순서 (1부터 시작)", example = "2", required = true)
        @NotNull(message = "새 순서는 필수입니다")
        @Min(value = 1, message = "순서는 1 이상이어야 합니다")
        Integer newOrder
) {
}
