package com.swcampus.api.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "배너 활성화 상태 변경 요청")
public record BannerActiveRequest(
        @Schema(description = "활성화 여부", example = "true", required = true) @NotNull(message = "활성화 여부는 필수입니다") Boolean isActive) {
}
