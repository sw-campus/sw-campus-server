package com.swcampus.domain.lecture.dto;

import com.swcampus.domain.lecture.Banner;
import com.swcampus.domain.lecture.BannerType;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.RecruitType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 배너 정보와 관련 강의 정보를 함께 담는 도메인 DTO.
 * 사용자용 배너 API에서 강의 상세 정보와 함께 반환합니다.
 */
@Getter
@Builder
public class BannerWithLectureDto {
    // Banner 정보
    private Long id;
    private Long lectureId;
    private BannerType type;
    private String url;
    private String imageUrl;
    private String backgroundColor;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Boolean isActive;

    // Lecture 정보
    private String lectureName;
    private LocalDateTime lectureStartAt;
    private LocalDateTime lectureDeadline;
    private RecruitType recruitType;
    private String orgName;

    public static BannerWithLectureDto from(Banner banner, Lecture lecture) {
        return BannerWithLectureDto.builder()
                .id(banner.getId())
                .lectureId(banner.getLectureId())
                .type(banner.getType())
                .url(banner.getUrl())
                .imageUrl(banner.getImageUrl())
                .backgroundColor(banner.getBackgroundColor())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .isActive(banner.getIsActive())
                .lectureName(lecture != null ? lecture.getLectureName() : null)
                .lectureStartAt(lecture != null ? lecture.getStartAt() : null)
                .lectureDeadline(lecture != null ? lecture.getDeadline() : null)
                .recruitType(lecture != null ? lecture.getRecruitType() : null)
                .orgName(lecture != null ? lecture.getOrgName() : null)
                .build();
    }
}
