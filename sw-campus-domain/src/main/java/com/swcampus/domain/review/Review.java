package com.swcampus.domain.review;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {
    private Long id;
    private Long memberId;
    private Long lectureId;
    private Long certificateId;
    private String comment;
    private Double score;
    private ApprovalStatus approvalStatus;
    private boolean blurred;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewDetail> details = new ArrayList<>();

    public static Review create(Long memberId, Long lectureId, Long certificateId,
                                 String comment, List<ReviewDetail> details) {
        Review review = new Review();
        review.memberId = memberId;
        review.lectureId = lectureId;
        review.certificateId = certificateId;
        review.comment = comment;
        review.details = details != null ? details : new ArrayList<>();
        review.score = calculateAverageScore(review.details);
        review.approvalStatus = ApprovalStatus.PENDING;
        review.blurred = false;
        review.createdAt = LocalDateTime.now();
        review.updatedAt = LocalDateTime.now();
        return review;
    }

    public static Review of(Long id, Long memberId, Long lectureId, Long certificateId,
                            String comment, Double score, ApprovalStatus approvalStatus,
                            boolean blurred, LocalDateTime createdAt, LocalDateTime updatedAt,
                            List<ReviewDetail> details) {
        Review review = new Review();
        review.id = id;
        review.memberId = memberId;
        review.lectureId = lectureId;
        review.certificateId = certificateId;
        review.comment = comment;
        review.score = score;
        review.approvalStatus = approvalStatus;
        review.blurred = blurred;
        review.createdAt = createdAt;
        review.updatedAt = updatedAt;
        review.details = details != null ? details : new ArrayList<>();
        return review;
    }

    public void update(String comment, List<ReviewDetail> details) {
        this.comment = comment;
        this.details = details != null ? details : new ArrayList<>();
        this.score = calculateAverageScore(this.details);
        this.updatedAt = LocalDateTime.now();
    }

    public void resubmit() {
        if (this.approvalStatus != ApprovalStatus.REJECTED) {
            throw new IllegalStateException("반려된 후기만 재제출할 수 있습니다.");
        }
        this.approvalStatus = ApprovalStatus.PENDING;
    }

    public void approve() {
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void reject() {
        this.approvalStatus = ApprovalStatus.REJECTED;
    }

    public void blind() {
        this.blurred = true;
    }

    public void unblind() {
        this.blurred = false;
    }

    public boolean isPending() {
        return this.approvalStatus == ApprovalStatus.PENDING;
    }

    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }

    private static Double calculateAverageScore(List<ReviewDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0.0;
        }
        double sum = details.stream()
                .map(ReviewDetail::getScore)
                .filter(score -> score != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        long count = details.stream()
                .map(ReviewDetail::getScore)
                .filter(score -> score != null)
                .count();
        if (count == 0) {
            return 0.0;
        }
        return Math.round(sum / count * 10) / 10.0;
    }
}
