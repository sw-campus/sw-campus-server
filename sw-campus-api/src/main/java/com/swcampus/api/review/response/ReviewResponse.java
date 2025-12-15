package com.swcampus.api.review.response;

import com.swcampus.domain.review.Review;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "후기 응답")
public record ReviewResponse(
    @Schema(description = "후기 ID", example = "1")
    Long reviewId,

    @Schema(description = "강의 ID", example = "1")
    Long lectureId,

    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "닉네임", example = "사용자닉네임")
    String nickname,

    @Schema(description = "총평", example = "전체적으로 만족스러운 강의였습니다.")
    String comment,

    @Schema(description = "평균 점수", example = "4.3")
    Double score,

    @Schema(description = "카테고리별 상세 점수")
    List<DetailScoreResponse> detailScores,

    @Schema(description = "승인 상태", example = "PENDING")
    String approvalStatus,

    @Schema(description = "블러 처리 여부", example = "false")
    boolean blurred,

    @Schema(description = "작성일시", example = "2025-12-10T10:30:00")
    String createdAt,

    @Schema(description = "수정일시", example = "2025-12-10T10:30:00")
    String updatedAt
) {
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static ReviewResponse from(Review review, String nickname) {
        List<DetailScoreResponse> detailScores = review.getDetails().stream()
                .map(detail -> new DetailScoreResponse(
                        detail.getCategory().name(),
                        detail.getScore(),
                        detail.getComment()
                ))
                .collect(Collectors.toList());

        return new ReviewResponse(
                review.getId(),
                review.getLectureId(),
                review.getMemberId(),
                nickname,
                review.getComment(),
                review.getScore(),
                detailScores,
                review.getApprovalStatus().name(),
                review.isBlurred(),
                review.getCreatedAt().format(FORMATTER),
                review.getUpdatedAt().format(FORMATTER)
        );
    }
}
