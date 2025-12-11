package com.swcampus.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTest {

    @Nested
    @DisplayName("Review 생성 테스트")
    class CreateReviewTest {

        @Test
        @DisplayName("후기 생성 시 평균 점수가 자동 계산된다")
        void createReview_calculatesAverageScore() {
            // given
            List<ReviewDetail> details = List.of(
                    ReviewDetail.create(ReviewCategory.TEACHER, 4.5, "강사님이 친절하고 설명을 잘 해주셨습니다."),
                    ReviewDetail.create(ReviewCategory.CURRICULUM, 4.0, "커리큘럼이 체계적이고 실무에 도움됩니다."),
                    ReviewDetail.create(ReviewCategory.MANAGEMENT, 4.5, "취업지원과 행정 서비스가 좋았습니다."),
                    ReviewDetail.create(ReviewCategory.FACILITY, 3.5, "시설은 보통이었지만 학습에는 문제없었습니다."),
                    ReviewDetail.create(ReviewCategory.PROJECT, 5.0, "프로젝트 경험이 정말 유익했습니다.")
            );

            // when
            Review review = Review.create(1L, 1L, 1L, "전체적으로 만족스러운 강의", details);

            // then
            assertThat(review.getScore()).isEqualTo(4.3);
            assertThat(review.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
            assertThat(review.isBlurred()).isFalse();
        }

        @Test
        @DisplayName("후기 생성 시 PENDING 상태로 생성된다")
        void createReview_statusIsPending() {
            // given
            List<ReviewDetail> details = createDefaultDetails();

            // when
            Review review = Review.create(1L, 1L, 1L, "테스트 후기", details);

            // then
            assertThat(review.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
            assertThat(review.isPending()).isTrue();
            assertThat(review.isApproved()).isFalse();
        }

        @Test
        @DisplayName("후기 생성 시 블라인드 처리되지 않은 상태로 생성된다")
        void createReview_notBlurred() {
            // given
            List<ReviewDetail> details = createDefaultDetails();

            // when
            Review review = Review.create(1L, 1L, 1L, "테스트 후기", details);

            // then
            assertThat(review.isBlurred()).isFalse();
        }

        @Test
        @DisplayName("상세 점수가 없으면 평균 점수는 0.0이다")
        void createReview_emptyDetails_scoreIsZero() {
            // when
            Review review = Review.create(1L, 1L, 1L, "테스트", List.of());

            // then
            assertThat(review.getScore()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("상세 점수가 null이면 평균 점수는 0.0이다")
        void createReview_nullDetails_scoreIsZero() {
            // when
            Review review = Review.create(1L, 1L, 1L, "테스트", null);

            // then
            assertThat(review.getScore()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Review 수정 테스트")
    class UpdateReviewTest {

        @Test
        @DisplayName("후기 수정 시 평균 점수가 재계산된다")
        void updateReview_recalculatesScore() {
            // given
            List<ReviewDetail> initialDetails = List.of(
                    ReviewDetail.create(ReviewCategory.TEACHER, 3.0, "보통이었습니다. 개선이 필요해요."),
                    ReviewDetail.create(ReviewCategory.CURRICULUM, 3.0, "커리큘럼이 조금 아쉬웠습니다."),
                    ReviewDetail.create(ReviewCategory.MANAGEMENT, 3.0, "행정 서비스가 보통이었습니다."),
                    ReviewDetail.create(ReviewCategory.FACILITY, 3.0, "시설이 좀 노후되었습니다."),
                    ReviewDetail.create(ReviewCategory.PROJECT, 3.0, "프로젝트 피드백이 부족했습니다.")
            );
            Review review = Review.create(1L, 1L, 1L, null, initialDetails);
            assertThat(review.getScore()).isEqualTo(3.0);

            List<ReviewDetail> newDetails = List.of(
                    ReviewDetail.create(ReviewCategory.TEACHER, 5.0, "강사님이 정말 최고였습니다!"),
                    ReviewDetail.create(ReviewCategory.CURRICULUM, 5.0, "커리큘럼이 완벽했습니다!"),
                    ReviewDetail.create(ReviewCategory.MANAGEMENT, 5.0, "취업지원이 정말 잘 됩니다!"),
                    ReviewDetail.create(ReviewCategory.FACILITY, 5.0, "시설이 깨끗하고 좋았습니다!"),
                    ReviewDetail.create(ReviewCategory.PROJECT, 5.0, "프로젝트가 정말 유익했습니다!")
            );

            // when
            review.update("정말 좋았습니다", newDetails);

            // then
            assertThat(review.getScore()).isEqualTo(5.0);
            assertThat(review.getComment()).isEqualTo("정말 좋았습니다");
        }

        @Test
        @DisplayName("후기 수정 시 comment가 변경된다")
        void updateReview_changesComment() {
            // given
            Review review = Review.create(1L, 1L, 1L, "원래 코멘트", createDefaultDetails());

            // when
            review.update("수정된 코멘트", createDefaultDetails());

            // then
            assertThat(review.getComment()).isEqualTo("수정된 코멘트");
        }
    }

    @Nested
    @DisplayName("Review 승인/반려 테스트")
    class ApprovalTest {

        @Test
        @DisplayName("후기 승인 시 상태가 APPROVED로 변경된다")
        void approveReview_changesStatusToApproved() {
            // given
            Review review = Review.create(1L, 1L, 1L, "테스트", createDefaultDetails());

            // when
            review.approve();

            // then
            assertThat(review.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(review.isApproved()).isTrue();
            assertThat(review.isPending()).isFalse();
        }

        @Test
        @DisplayName("후기 반려 시 상태가 REJECTED로 변경된다")
        void rejectReview_changesStatusToRejected() {
            // given
            Review review = Review.create(1L, 1L, 1L, "테스트", createDefaultDetails());

            // when
            review.reject();

            // then
            assertThat(review.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
            assertThat(review.isApproved()).isFalse();
            assertThat(review.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("Review 블라인드 테스트")
    class BlindTest {

        @Test
        @DisplayName("후기 블라인드 처리")
        void blindReview_setsBlurredTrue() {
            // given
            Review review = Review.create(1L, 1L, 1L, "테스트", createDefaultDetails());

            // when
            review.blind();

            // then
            assertThat(review.isBlurred()).isTrue();
        }

        @Test
        @DisplayName("후기 블라인드 해제")
        void unblindReview_setsBlurredFalse() {
            // given
            Review review = Review.create(1L, 1L, 1L, "테스트", createDefaultDetails());
            review.blind();
            assertThat(review.isBlurred()).isTrue();

            // when
            review.unblind();

            // then
            assertThat(review.isBlurred()).isFalse();
        }
    }

    @Nested
    @DisplayName("평균 점수 계산 테스트")
    class AverageScoreCalculationTest {

        @Test
        @DisplayName("소수점 첫째 자리까지 반올림된다")
        void calculateAverageScore_roundsToOneDecimal() {
            // given: 평균 4.3 (21.5 / 5)
            List<ReviewDetail> details = List.of(
                    ReviewDetail.create(ReviewCategory.TEACHER, 4.5, "테스트"),
                    ReviewDetail.create(ReviewCategory.CURRICULUM, 4.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.MANAGEMENT, 4.5, "테스트"),
                    ReviewDetail.create(ReviewCategory.FACILITY, 3.5, "테스트"),
                    ReviewDetail.create(ReviewCategory.PROJECT, 5.0, "테스트")
            );

            // when
            Review review = Review.create(1L, 1L, 1L, "테스트", details);

            // then
            assertThat(review.getScore()).isEqualTo(4.3);
        }

        @Test
        @DisplayName("모든 점수가 동일하면 해당 점수가 평균이 된다")
        void calculateAverageScore_allSameScores() {
            // given
            List<ReviewDetail> details = List.of(
                    ReviewDetail.create(ReviewCategory.TEACHER, 4.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.CURRICULUM, 4.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.MANAGEMENT, 4.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.FACILITY, 4.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.PROJECT, 4.0, "테스트")
            );

            // when
            Review review = Review.create(1L, 1L, 1L, "테스트", details);

            // then
            assertThat(review.getScore()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("최고 점수 5.0일 때 정확히 계산된다")
        void calculateAverageScore_maxScore() {
            // given
            List<ReviewDetail> details = List.of(
                    ReviewDetail.create(ReviewCategory.TEACHER, 5.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.CURRICULUM, 5.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.MANAGEMENT, 5.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.FACILITY, 5.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.PROJECT, 5.0, "테스트")
            );

            // when
            Review review = Review.create(1L, 1L, 1L, "테스트", details);

            // then
            assertThat(review.getScore()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("최저 점수 1.0일 때 정확히 계산된다")
        void calculateAverageScore_minScore() {
            // given
            List<ReviewDetail> details = List.of(
                    ReviewDetail.create(ReviewCategory.TEACHER, 1.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.CURRICULUM, 1.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.MANAGEMENT, 1.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.FACILITY, 1.0, "테스트"),
                    ReviewDetail.create(ReviewCategory.PROJECT, 1.0, "테스트")
            );

            // when
            Review review = Review.create(1L, 1L, 1L, "테스트", details);

            // then
            assertThat(review.getScore()).isEqualTo(1.0);
        }
    }

    // 테스트 헬퍼 메서드
    private List<ReviewDetail> createDefaultDetails() {
        return List.of(
                ReviewDetail.create(ReviewCategory.TEACHER, 4.0, "강사님이 좋았습니다."),
                ReviewDetail.create(ReviewCategory.CURRICULUM, 4.0, "커리큘럼이 좋았습니다."),
                ReviewDetail.create(ReviewCategory.MANAGEMENT, 4.0, "행정이 좋았습니다."),
                ReviewDetail.create(ReviewCategory.FACILITY, 4.0, "시설이 좋았습니다."),
                ReviewDetail.create(ReviewCategory.PROJECT, 4.0, "프로젝트가 좋았습니다.")
        );
    }
}
