package com.swcampus.api.mypage.response;

import com.swcampus.domain.mypage.dto.CompletedLectureInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 수강 완료 강의 응답 (수료증 인증 완료)")
public record MyCompletedLectureResponse(
    @Schema(description = "수료증 ID")
    Long certificateId,

    @Schema(description = "강의 ID")
    Long lectureId,

    @Schema(description = "강의명")
    String lectureName,

    @Schema(description = "강의 이미지 URL")
    String lectureImageUrl,

    @Schema(description = "기관명")
    String organizationName,

    @Schema(description = "수료 인증일")
    LocalDateTime certifiedAt,

    @Schema(description = "후기 작성 가능 여부")
    Boolean canWriteReview
) {
    public static MyCompletedLectureResponse from(CompletedLectureInfo info) {
        return new MyCompletedLectureResponse(
            info.certificateId(),
            info.lectureId(),
            info.lectureName(),
            info.lectureImageUrl(),
            info.organizationName(),
            info.certifiedAt(),
            info.canWriteReview()
        );
    }
}
