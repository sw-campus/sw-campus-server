package com.swcampus.api.admin.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자용 후기 상세 응답")
public record AdminReviewDetailResponse(
    @Schema(description = "후기 ID", example = "1")
    Long reviewId,

    @Schema(description = "강의 ID", example = "10")
    Long lectureId,

    @Schema(description = "강의명", example = "웹 개발 부트캠프")
    String lectureName,

    @Schema(description = "회원 ID", example = "5")
    Long memberId,

    @Schema(description = "사용자 이름", example = "김철수")
    String userName,

    @Schema(description = "닉네임", example = "개발자123")
    String nickname,

    @Schema(description = "후기 내용", example = "좋은 강의였습니다.")
    String comment,

    @Schema(description = "종합 평점", example = "4.5")
    Double score,

    @Schema(description = "승인 상태", example = "PENDING")
    String approvalStatus,

    @Schema(description = "수료증 ID", example = "3")
    Long certificateId,

    @Schema(description = "수료증 승인 상태", example = "APPROVED")
    String certificateApprovalStatus,

    @Schema(description = "세부 평점 목록")
    List<DetailScore> detailScores,

    @Schema(description = "작성일", example = "2025-01-15T10:30:00")
    String createdAt
) {
    @Schema(description = "세부 평점")
    public record DetailScore(
        @Schema(description = "평가 항목", example = "CURRICULUM")
        String category,

        @Schema(description = "평점", example = "4.0")
        Double score,

        @Schema(description = "코멘트", example = "커리큐럼이 알찼습니다.")
        String comment
    ) {}
}
