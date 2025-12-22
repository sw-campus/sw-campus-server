package com.swcampus.infra.postgres.review;

import com.swcampus.domain.review.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewCategory;
import com.swcampus.domain.review.ReviewDetail;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reviews_seq")
    @SequenceGenerator(name = "reviews_seq", sequenceName = "reviews_review_id_seq", allocationSize = 1)
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

    /**
     * 기존 Entity의 값을 업데이트합니다.
     * JPA Auditing이 @LastModifiedDate를 자동으로 갱신합니다.
     */
    public void update(Review review) {
        this.comment = review.getComment();
        this.score = review.getScore();
        this.approvalStatus = review.getApprovalStatus();
        this.blurred = review.isBlurred();

        // 기존 details를 category 기준으로 업데이트, 추가, 삭제 처리
        if (review.getDetails() != null) {
            Map<ReviewCategory, ReviewDetail> newDetailsMap = review.getDetails().stream()
                    .collect(Collectors.toMap(ReviewDetail::getCategory, d -> d));

            // 기존 details 업데이트 또는 삭제 대상 수집
            List<ReviewDetailEntity> toRemove = new ArrayList<>();
            for (ReviewDetailEntity existingDetail : this.details) {
                ReviewDetail newDetail = newDetailsMap.get(existingDetail.getCategory());
                if (newDetail != null) {
                    existingDetail.update(newDetail);
                    newDetailsMap.remove(existingDetail.getCategory());
                } else {
                    toRemove.add(existingDetail);
                }
            }
            this.details.removeAll(toRemove);

            // 새로운 카테고리 추가
            for (ReviewDetail newDetail : newDetailsMap.values()) {
                ReviewDetailEntity newEntity = ReviewDetailEntity.from(newDetail);
                newEntity.setReview(this);
                this.details.add(newEntity);
            }
        } else {
            this.details.clear();
        }
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
