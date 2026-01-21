package com.swcampus.api.banner.response;

import com.swcampus.domain.lecture.BannerType;
import com.swcampus.domain.lecture.RecruitType;
import com.swcampus.domain.lecture.dto.BannerWithLectureDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Schema(description = "배너 응답")
public record BannerResponse(
        @Schema(description = "배너 ID", example = "1") Long id,

        @Schema(description = "강의 ID", example = "100") Long lectureId,

        @Schema(description = "배너 타입", example = "BIG") BannerType type,

        @Schema(description = "배너 클릭 시 이동 URL", example = "https://example.com/event") String url,

        @Schema(description = "배너 이미지 URL", example = "https://example.com/banner.jpg") String imageUrl,

        @Schema(description = "배너 배경색 (HEX)", example = "#FF5733") String backgroundColor,

        @Schema(description = "시작일") OffsetDateTime startDate,

        @Schema(description = "종료일") OffsetDateTime endDate,

        @Schema(description = "활성화 여부", example = "true") Boolean isActive,

        // 강의 정보
        @Schema(description = "강의명", example = "Java 백엔드 부트캠프") String lectureName,

        @Schema(description = "강의 시작일") LocalDateTime lectureStartAt,

        @Schema(description = "모집 마감일") LocalDateTime lectureDeadline,

        @Schema(description = "내배카 필요여부", example = "CARD_REQUIRED") RecruitType recruitType,

        @Schema(description = "기관명", example = "스파르타코딩클럽") String orgName) {

    public static BannerResponse from(BannerWithLectureDto dto) {
        return new BannerResponse(
                dto.getId(),
                dto.getLectureId(),
                dto.getType(),
                dto.getUrl(),
                dto.getImageUrl(),
                dto.getBackgroundColor(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getIsActive(),
                dto.getLectureName(),
                dto.getLectureStartAt(),
                dto.getLectureDeadline(),
                dto.getRecruitType(),
                dto.getOrgName());
    }
}
