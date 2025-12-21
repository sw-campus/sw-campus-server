package com.swcampus.domain.lecture.dto;

import com.swcampus.domain.lecture.Banner;
import com.swcampus.domain.lecture.BannerType;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * 배너 정보와 관련 강의명을 함께 담는 도메인 DTO.
 * AdminBannerService에서 배너와 강의명을 조합하여 반환합니다.
 */
@Getter
@Builder
public class BannerDetailsDto {
    private Long id;
    private Long lectureId;
    private String lectureName;
    private BannerType type;
    private String content;
    private String imageUrl;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Boolean isActive;

    /**
     * Banner 도메인 객체와 강의명을 조합하여 BannerDetailsDto를 생성합니다.
     */
    public static BannerDetailsDto from(Banner banner, String lectureName) {
        return BannerDetailsDto.builder()
                .id(banner.getId())
                .lectureId(banner.getLectureId())
                .lectureName(lectureName)
                .type(banner.getType())
                .content(banner.getContent())
                .imageUrl(banner.getImageUrl())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .isActive(banner.getIsActive())
                .build();
    }
}
