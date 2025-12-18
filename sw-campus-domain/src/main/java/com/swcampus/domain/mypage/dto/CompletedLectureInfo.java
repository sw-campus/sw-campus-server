package com.swcampus.domain.mypage.dto;

import java.time.LocalDateTime;

/**
 * 수강 완료 강의 정보 DTO (수료증 인증 완료된 강의)
 */
public record CompletedLectureInfo(
    Long certificateId,
    Long lectureId,
    String lectureName,
    String lectureImageUrl,
    String organizationName,
    LocalDateTime certifiedAt,
    boolean canWriteReview
) {
}
