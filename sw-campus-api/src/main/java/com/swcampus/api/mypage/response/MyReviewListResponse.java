package com.swcampus.api.mypage.response;

import com.swcampus.domain.review.ApprovalStatus;
import com.swcampus.domain.review.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 후기 목록 응답")
public record MyReviewListResponse(
    @Schema(description = "후기 ID")
    Long reviewId,

    @Schema(description = "강의 ID")
    Long lectureId,

    @Schema(description = "강의명")
    String lectureName,

    @Schema(description = "평점")
    Double score,

    @Schema(description = "내용")
    String content,

    @Schema(description = "승인 상태")
    ApprovalStatus approvalStatus,

    @Schema(description = "작성일")
    LocalDateTime createdAt,

    @Schema(description = "수정일")
    LocalDateTime updatedAt,

    @Schema(description = "수정 가능 여부")
    Boolean canEdit
) {
    public static MyReviewListResponse from(Review review, String lectureName) {
        return new MyReviewListResponse(
            review.getId(),
            review.getLectureId(),
            lectureName,
            review.getScore(),
            review.getComment(),
            review.getApprovalStatus(),
            review.getCreatedAt(),
            review.getUpdatedAt(),
            review.getApprovalStatus() == ApprovalStatus.REJECTED
        );
    }
}
