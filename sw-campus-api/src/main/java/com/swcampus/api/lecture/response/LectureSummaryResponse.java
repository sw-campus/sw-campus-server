package com.swcampus.api.lecture.response;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.lecture.dto.LectureSummaryDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 요약 정보 응답 (목록 조회용)")
public record LectureSummaryResponse(
        @Schema(description = "강의 ID", example = "1") Long lectureId,

        @Schema(description = "기관 ID", example = "1") Long orgId,

        @Schema(description = "기관명", example = "패스트캠퍼스") String orgName,

        @Schema(description = "강의명", example = "웹 개발 부트캠프") String lectureName,

        @Schema(description = "수업 요일", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]") Set<String> days,

        @Schema(description = "수업 장소 유형", example = "OFFLINE") String lectureLoc,

        @Schema(description = "수업 장소 주소", example = "서울시 강남구 테헤란로 123") String location,

        @Schema(description = "모집 유형", example = "GOVERNMENT_FUNDED") String recruitType,

        @Schema(description = "강의 상태 (RECRUITING, FINISHED)", example = "RECRUITING") String status,

        @Schema(description = "관리자 승인 상태 (PENDING, APPROVED, REJECTED)", example = "APPROVED") String lectureAuthStatus,

        @Schema(description = "교육 시작일", example = "2025-03-01T00:00:00") String startAt,

        @Schema(description = "교육 종료일", example = "2025-08-31T00:00:00") String endAt,

        @Schema(description = "모집 마감일", example = "2025-02-15T00:00:00") String deadline,

        @Schema(description = "카테고리명", example = "백엔드") String categoryName,

        @Schema(description = "리뷰 평균 점수", example = "4.5") Double averageScore,

        @Schema(description = "리뷰 수", example = "10") Long reviewCount,

        @Schema(description = "총 교육 일수", example = "120") Integer totalDays,

        @Schema(description = "총 교육 시간", example = "960") Integer totalTimes,

        @Schema(description = "강의료 (자부담금)", example = "500000") java.math.BigDecimal lectureFee,

        @Schema(description = "정부 지원금", example = "3000000") java.math.BigDecimal subsidy,

        @Schema(description = "훈련 수당", example = "100000") java.math.BigDecimal eduSubsidy,

        @Schema(description = "선발 절차 목록") List<StepResponse> steps) {

    public static LectureSummaryResponse from(Lecture lecture, Double averageScore, Long reviewCount) {
        return from(lecture, null, averageScore, reviewCount);
    }

    public static LectureSummaryResponse from(LectureSummaryDto dto, String orgName) {
        return from(dto.lecture(), orgName, dto.averageScore(), dto.reviewCount());
    }

    public static LectureSummaryResponse from(Lecture lecture, String orgName, Double averageScore, Long reviewCount) {
        return new LectureSummaryResponse(
                lecture.getLectureId(),
                lecture.getOrgId(),
                orgName != null ? orgName : lecture.getOrgName(),
                lecture.getLectureName(),
                lecture.getDays() != null
                        ? lecture.getDays().stream().map(Enum::name).collect(Collectors.toSet())
                        : Collections.emptySet(),
                lecture.getLectureLoc() != null ? lecture.getLectureLoc().name() : null,
                lecture.getLocation(),
                lecture.getRecruitType() != null ? lecture.getRecruitType().name() : null,
                lecture.getStatus().name(),
                lecture.getLectureAuthStatus() != null ? lecture.getLectureAuthStatus().name() : null,
                lecture.getStartAt() != null ? lecture.getStartAt().toString() : null,
                lecture.getEndAt() != null ? lecture.getEndAt().toString() : null,
                lecture.getDeadline() != null ? lecture.getDeadline().toString() : null,
                extractCategoryName(lecture),
                averageScore != null ? averageScore : lecture.getAverageScore(),
                reviewCount != null ? reviewCount : lecture.getReviewCount(),
                lecture.getTotalDays(),
                lecture.getTotalTimes(),
                lecture.getLectureFee(),
                lecture.getSubsidy(),
                lecture.getEduSubsidy(),
                lecture.getSteps() != null ? lecture.getSteps().stream().map(StepResponse::from).toList() : List.of());
    }

    private static String extractCategoryName(Lecture lecture) {
        // If categoryName is already set (from MyBatis search result), use it
        if (lecture.getCategoryName() != null && !lecture.getCategoryName().isBlank()) {
            return lecture.getCategoryName();
        }

        // Otherwise, extract from lectureCurriculums (for JPA detail query)
        if (lecture.getLectureCurriculums() == null || lecture.getLectureCurriculums().isEmpty()) {
            return null;
        }
        var firstCurriculum = lecture.getLectureCurriculums().get(0).getCurriculum();
        if (firstCurriculum == null || firstCurriculum.getCategory() == null) {
            return null;
        }
        return firstCurriculum.getCategory().getCategoryName();
    }

    @Schema(description = "선발 절차 응답")
    public record StepResponse(
            @Schema(description = "절차 ID", example = "1") Long stepId,

            @Schema(description = "절차 유형", example = "INTERVIEW") String stepType,

            @Schema(description = "절차 순서", example = "1") Integer stepOrder) {
        public static StepResponse from(LectureStep s) {
            return new StepResponse(s.getStepId(), s.getStepType() != null ? s.getStepType().name() : null,
                    s.getStepOrder());
        }
    }
}
