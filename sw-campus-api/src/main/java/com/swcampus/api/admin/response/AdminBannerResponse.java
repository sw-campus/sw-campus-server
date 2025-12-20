package com.swcampus.api.admin.response;

import com.swcampus.domain.lecture.dto.BannerDetailsDto;
import com.swcampus.domain.lecture.BannerType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "관리자 배너 응답")
public record AdminBannerResponse(
        @Schema(description = "배너 ID", example = "1") Long id,

        @Schema(description = "강의 ID", example = "100") Long lectureId,

        @Schema(description = "강의명", example = "Java 백엔드 부트캠프") String lectureName,

        @Schema(description = "배너 타입", example = "BIG") BannerType type,

        @Schema(description = "배너 내용", example = "신규 강의 런칭!") String content,

        @Schema(description = "배너 이미지 URL", example = "https://example.com/banner.jpg") String imageUrl,

        @Schema(description = "시작일") OffsetDateTime startDate,

        @Schema(description = "종료일") OffsetDateTime endDate,

        @Schema(description = "활성화 여부", example = "true") Boolean isActive) {

    public static AdminBannerResponse from(BannerDetailsDto details) {
        return new AdminBannerResponse(
                details.getId(),
                details.getLectureId(),
                details.getLectureName(),
                details.getType(),
                details.getContent(),
                details.getImageUrl(),
                details.getStartDate(),
                details.getEndDate(),
                details.getIsActive());
    }
}
