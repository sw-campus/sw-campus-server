package com.swcampus.infra.postgres.review;

import com.swcampus.domain.review.ReviewCategory;
import com.swcampus.domain.review.ReviewDetail;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reviews_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reviews_details_seq")
    @SequenceGenerator(name = "reviews_details_seq", sequenceName = "reviews_details_review_detail_id_seq", allocationSize = 1)
    @Column(name = "review_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @Setter
    private ReviewEntity review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewCategory category;

    @Column(nullable = false)
    private Double score;

    private String comment;

    public static ReviewDetailEntity from(ReviewDetail detail) {
        ReviewDetailEntity entity = new ReviewDetailEntity();
        entity.id = detail.getId();
        entity.category = detail.getCategory();
        entity.score = detail.getScore();
        entity.comment = detail.getComment();
        return entity;
    }

    public ReviewDetail toDomain() {
        return ReviewDetail.of(
            this.id,
            this.review != null ? this.review.getId() : null,
            this.category,
            this.score,
            this.comment
        );
    }

    public void update(ReviewDetail detail) {
        this.score = detail.getScore();
        this.comment = detail.getComment();
    }
}
