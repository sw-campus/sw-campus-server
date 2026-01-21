package com.swcampus.api.mypage.response;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 강의 목록 응답")
public record MyLectureListResponse(
    @Schema(description = "강의 ID")
    Long lectureId,

    @Schema(description = "강의명")
    String lectureName,

    @Schema(description = "강의 이미지 URL")
    String lectureImageUrl,

    @Schema(description = "승인 상태")
    LectureAuthStatus lectureAuthStatus,

    @Schema(description = "모집 상태")
    LectureStatus status,

    @Schema(description = "생성일")
    LocalDateTime createdAt,

    @Schema(description = "수정일")
    LocalDateTime updatedAt,

    @Schema(description = "수정 가능 여부")
    Boolean canEdit
) {
    public static MyLectureListResponse from(Lecture lecture) {
        return new MyLectureListResponse(
            lecture.getLectureId(),
            lecture.getLectureName(),
            lecture.getLectureImageUrl(),
            lecture.getLectureAuthStatus(),
            lecture.getStatus(),
            lecture.getCreatedAt(),
            lecture.getUpdatedAt(),
            lecture.getLectureAuthStatus() == LectureAuthStatus.REJECTED
        );
    }
}
