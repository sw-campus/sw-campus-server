package com.swcampus.infra.postgres.review;

import com.swcampus.domain.review.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewDetail;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long memberId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "certificate_id", nullable = false)
    private Long certificateId;

    @Column(nullable = false)
    private String comment;

    private Double score;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    private boolean blurred;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewDetailEntity> details = new ArrayList<>();

    public static ReviewEntity from(Review review) {
        ReviewEntity entity = new ReviewEntity();
        entity.id = review.getId();
        entity.memberId = review.getMemberId();
        entity.lectureId = review.getLectureId();
        entity.certificateId = review.getCertificateId();
        entity.comment = review.getComment();
        entity.score = review.getScore();
        entity.approvalStatus = review.getApprovalStatus();
        entity.blurred = review.isBlurred();

        if (review.getDetails() != null) {
            for (ReviewDetail detail : review.getDetails()) {
                ReviewDetailEntity detailEntity = ReviewDetailEntity.from(detail);
                detailEntity.setReview(entity);
                entity.details.add(detailEntity);
            }
        }

        return entity;
    }

    public Review toDomain() {
        List<ReviewDetail> domainDetails = this.details.stream()
            .map(ReviewDetailEntity::toDomain)
            .toList();

        return Review.of(
            this.id,
            this.memberId,
            this.lectureId,
            this.certificateId,
            this.comment,
            this.score,
            this.approvalStatus,
            this.blurred,
            this.getCreatedAt(),
            this.getUpdatedAt(),
            domainDetails
        );
    }
}
