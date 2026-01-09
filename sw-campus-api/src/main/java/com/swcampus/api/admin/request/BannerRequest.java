package com.swcampus.api.admin.request;

import com.swcampus.domain.lecture.Banner;
import com.swcampus.domain.lecture.BannerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "배너 생성/수정 요청")
public record BannerRequest(
        @Schema(description = "강의 ID", example = "1", required = true) @NotNull(message = "강의 ID는 필수입니다") Long lectureId,

        @Schema(description = "배너 타입", example = "BIG", required = true) @NotNull(message = "배너 타입은 필수입니다") BannerType type,

        @Schema(description = "배너 클릭 시 이동 URL", example = "https://example.com/event") @org.hibernate.validator.constraints.URL(message = "유효한 URL 형식이 아닙니다.") String url,

        @Schema(description = "배너 이미지 URL", example = "https://example.com/banner.jpg") String imageUrl,

        @Schema(description = "배너 배경색 (HEX)", example = "#FF5733") String backgroundColor,

        @Schema(description = "시작일", required = true) @NotNull(message = "시작일은 필수입니다") OffsetDateTime startDate,

        @Schema(description = "종료일", required = true) @NotNull(message = "종료일은 필수입니다") OffsetDateTime endDate,

        @Schema(description = "활성화 여부", example = "true") Boolean isActive) {
    public Banner toDomain() {
        return Banner.builder()
                .lectureId(lectureId)
                .type(type)
                .url(url)
                .imageUrl(imageUrl)
                .backgroundColor(backgroundColor)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(isActive)
                .build();
    }
}
