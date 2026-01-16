package com.swcampus.api.lecture.response;

import java.time.LocalDateTime;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAuthStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 강의 목록 요약 응답")
public class AdminLectureSummaryResponse {
    @Schema(description = "강의 ID", example = "1")
    private Long lectureId;

    @Schema(description = "강의명", example = "웹 개발 부트캠프")
    private String lectureName;

    @Schema(description = "기관명", example = "테스트교육기관")
    private String orgName;

    @Schema(description = "승인 상태", example = "PENDING")
    private LectureAuthStatus lectureAuthStatus;

    @Schema(description = "신청 또는 최종 수정일시", example = "2025-12-15T10:00:00")
    private LocalDateTime lastUpdatedAt;

    public static AdminLectureSummaryResponse from(Lecture lecture) {
        // updatedAt이 있으면 updatedAt을, 없으면 createdAt을 사용
        LocalDateTime displayDate = lecture.getUpdatedAt() != null
                ? lecture.getUpdatedAt()
                : lecture.getCreatedAt();

        return AdminLectureSummaryResponse.builder()
                .lectureId(lecture.getLectureId())
                .lectureName(lecture.getLectureName())
                .orgName(lecture.getOrgName())
                .lectureAuthStatus(lecture.getLectureAuthStatus())
                .lastUpdatedAt(displayDate)
                .build();
    }
}
