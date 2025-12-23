package com.swcampus.domain.review;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.certificate.exception.CertificateNotVerifiedException;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.review.exception.ReviewAlreadyExistsException;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import com.swcampus.domain.review.exception.ReviewNotModifiableException;
import com.swcampus.domain.review.exception.ReviewNotOwnerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("후기 작성 가능 여부 확인")
    class CheckEligibilityTest {

        @Test
        @DisplayName("모든 조건 충족 시 eligible = true")
        void checkEligibility_allConditionsMet_eligible() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Member member = createMemberWithNickname("홍길동");

            given(memberRepository.findById(memberId))
                    .willReturn(Optional.of(member));
            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(true);
            given(reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);

            // when
            ReviewEligibility eligibility = reviewService.checkEligibility(memberId, lectureId);

            // then
            assertThat(eligibility.eligible()).isTrue();
            assertThat(eligibility.hasNickname()).isTrue();
            assertThat(eligibility.hasCertificate()).isTrue();
            assertThat(eligibility.canWrite()).isTrue();
        }

        @Test
        @DisplayName("닉네임 없으면 eligible = false")
        void checkEligibility_noNickname_notEligible() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Member member = createMemberWithNickname(null);

            given(memberRepository.findById(memberId))
                    .willReturn(Optional.of(member));
            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(true);
            given(reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);

            // when
            ReviewEligibility eligibility = reviewService.checkEligibility(memberId, lectureId);

            // then
            assertThat(eligibility.eligible()).isFalse();
            assertThat(eligibility.hasNickname()).isFalse();
        }

        @Test
        @DisplayName("빈 닉네임이면 eligible = false")
        void checkEligibility_blankNickname_notEligible() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Member member = createMemberWithNickname("   ");

            given(memberRepository.findById(memberId))
                    .willReturn(Optional.of(member));
            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(true);
            given(reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);

            // when
            ReviewEligibility eligibility = reviewService.checkEligibility(memberId, lectureId);

            // then
            assertThat(eligibility.eligible()).isFalse();
            assertThat(eligibility.hasNickname()).isFalse();
        }

        @Test
        @DisplayName("수료증 없으면 eligible = false")
        void checkEligibility_noCertificate_notEligible() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Member member = createMemberWithNickname("홍길동");

            given(memberRepository.findById(memberId))
                    .willReturn(Optional.of(member));
            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);

            // when
            ReviewEligibility eligibility = reviewService.checkEligibility(memberId, lectureId);

            // then
            assertThat(eligibility.eligible()).isFalse();
            assertThat(eligibility.hasCertificate()).isFalse();
        }

        @Test
        @DisplayName("이미 후기 작성했으면 eligible = false")
        void checkEligibility_alreadyWritten_notEligible() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            Member member = createMemberWithNickname("홍길동");

            given(memberRepository.findById(memberId))
                    .willReturn(Optional.of(member));
            given(certificateRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(true);
            given(reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(true);

            // when
            ReviewEligibility eligibility = reviewService.checkEligibility(memberId, lectureId);

            // then
            assertThat(eligibility.eligible()).isFalse();
            assertThat(eligibility.canWrite()).isFalse();
        }

        @Test
        @DisplayName("회원이 없으면 예외 발생")
        void checkEligibility_memberNotFound_throwsException() {
            // given
            Long memberId = 999L;
            Long lectureId = 1L;

            given(memberRepository.findById(memberId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.checkEligibility(memberId, lectureId))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("후기 작성")
    class CreateReviewTest {

        @Test
        @DisplayName("정상적으로 후기 작성 성공")
        void createReview_success() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;
            String comment = "좋은 강의였습니다";
            List<ReviewDetail> details = createDefaultDetails();

            Certificate certificate = Certificate.create(memberId, lectureId, "https://s3.../image.jpg", "SUCCESS");

            given(certificateRepository.findByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(Optional.of(certificate));
            given(reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(false);
            given(reviewRepository.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.createReview(memberId, lectureId, comment, details);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getLectureId()).isEqualTo(lectureId);
            assertThat(result.getComment()).isEqualTo(comment);
            assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("수료증 없이 후기 작성 시 예외 발생")
        void createReview_noCertificate_throwsException() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            given(certificateRepository.findByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(
                    memberId, lectureId, "테스트", List.of()
            )).isInstanceOf(CertificateNotVerifiedException.class);
        }

        @Test
        @DisplayName("이미 후기가 있는 강의에 작성 시 예외 발생")
        void createReview_alreadyExists_throwsException() {
            // given
            Long memberId = 1L;
            Long lectureId = 1L;

            Certificate certificate = Certificate.create(memberId, lectureId, "https://s3.../image.jpg", "SUCCESS");

            given(certificateRepository.findByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(Optional.of(certificate));
            given(reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(
                    memberId, lectureId, "테스트", List.of()
            )).isInstanceOf(ReviewAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("후기 수정")
    class UpdateReviewTest {

        @Test
        @DisplayName("반려된 후기 수정 성공 및 상태 변경(PENDING)")
        void updateReview_rejected_success() {
            // given
            Long memberId = 1L;
            Long reviewId = 1L;
            String newComment = "수정된 후기";
            List<ReviewDetail> newDetails = createDefaultDetails();

            Review review = Review.create(memberId, 1L, 1L, "원래 후기", createDefaultDetails());
            review.reject(); // REJECTED 상태로 설정

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));
            given(reviewRepository.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.updateReview(memberId, reviewId, newComment, newDetails);

            // then
            assertThat(result.getComment()).isEqualTo(newComment);
            assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("후기가 없으면 예외 발생")
        void updateReview_notFound_throwsException() {
            // given
            Long memberId = 1L;
            Long reviewId = 999L;

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(
                    memberId, reviewId, "테스트", List.of()
            )).isInstanceOf(ReviewNotFoundException.class);
        }

        @Test
        @DisplayName("본인 후기가 아니면 예외 발생")
        void updateReview_notOwner_throwsException() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long reviewId = 1L;

            Review review = Review.create(otherMemberId, 1L, 1L, "후기", createDefaultDetails());

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(
                    memberId, reviewId, "테스트", List.of()
            )).isInstanceOf(ReviewNotOwnerException.class);
        }

        @Test
        @DisplayName("승인된 후기 수정 시 예외 발생")
        void updateReview_alreadyApproved_throwsException() {
            // given
            Long memberId = 1L;
            Long reviewId = 1L;

            Review review = Review.create(memberId, 1L, 1L, "후기", createDefaultDetails());
            review.approve(); // 승인 처리

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(
                    memberId, reviewId, "테스트", List.of()
            )).isInstanceOf(ReviewNotModifiableException.class);
        }

        @Test
        @DisplayName("대기중(PENDING)인 후기 수정 성공")
        void updateReview_pending_success() {
            // given
            Long memberId = 1L;
            Long reviewId = 1L;

            Review review = Review.create(memberId, 1L, 1L, "후기", createDefaultDetails());
            // Default is PENDING

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));
            given(reviewRepository.save(any(Review.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Review result = reviewService.updateReview(
                    memberId, reviewId, "수정된 후기", createDefaultDetails()
            );

            // then
            assertThat(result.getComment()).isEqualTo("수정된 후기");
            assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("후기 조회")
    class GetReviewTest {

        @Test
        @DisplayName("후기 상세 조회 성공")
        void getReview_success() {
            // given
            Long reviewId = 1L;
            Review review = Review.of(
                    reviewId, 1L, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.APPROVED, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));

            // when
            Review result = reviewService.getReview(reviewId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("후기가 없으면 예외 발생")
        void getReview_notFound_throwsException() {
            // given
            Long reviewId = 999L;

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.getReview(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("후기 상세 조회 (접근 제어)")
    class GetReviewWithNicknameTest {

        @Test
        @DisplayName("본인의 PENDING 후기 조회 성공")
        void getReviewWithNickname_owner_pending_success() {
            // given
            Long reviewId = 1L;
            Long memberId = 1L;
            Review review = Review.of(
                    reviewId, memberId, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.PENDING, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );
            Member member = createMemberWithNickname("홍길동");

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));
            given(memberRepository.findById(memberId))
                    .willReturn(Optional.of(member));

            // when
            ReviewWithNickname result = reviewService.getReviewWithNickname(reviewId, memberId);

            // then
            assertThat(result.review().getId()).isEqualTo(reviewId);
            assertThat(result.nickname()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("본인의 REJECTED 후기 조회 성공")
        void getReviewWithNickname_owner_rejected_success() {
            // given
            Long reviewId = 1L;
            Long memberId = 1L;
            Review review = Review.of(
                    reviewId, memberId, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.REJECTED, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );
            Member member = createMemberWithNickname("홍길동");

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));
            given(memberRepository.findById(memberId))
                    .willReturn(Optional.of(member));

            // when
            ReviewWithNickname result = reviewService.getReviewWithNickname(reviewId, memberId);

            // then
            assertThat(result.review().getId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("타인의 APPROVED 후기 조회 성공")
        void getReviewWithNickname_otherUser_approved_success() {
            // given
            Long reviewId = 1L;
            Long ownerId = 1L;
            Long requesterId = 2L;
            Review review = Review.of(
                    reviewId, ownerId, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.APPROVED, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );
            Member owner = createMemberWithNickname("홍길동");

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));
            given(memberRepository.findById(ownerId))
                    .willReturn(Optional.of(owner));

            // when
            ReviewWithNickname result = reviewService.getReviewWithNickname(reviewId, requesterId);

            // then
            assertThat(result.review().getId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("미인증 사용자의 APPROVED 후기 조회 성공")
        void getReviewWithNickname_unauthenticated_approved_success() {
            // given
            Long reviewId = 1L;
            Long ownerId = 1L;
            Review review = Review.of(
                    reviewId, ownerId, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.APPROVED, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );
            Member owner = createMemberWithNickname("홍길동");

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));
            given(memberRepository.findById(ownerId))
                    .willReturn(Optional.of(owner));

            // when (null requesterId = 미인증)
            ReviewWithNickname result = reviewService.getReviewWithNickname(reviewId, null);

            // then
            assertThat(result.review().getId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("타인의 PENDING 후기 조회 시 예외 발생")
        void getReviewWithNickname_otherUser_pending_throwsException() {
            // given
            Long reviewId = 1L;
            Long ownerId = 1L;
            Long requesterId = 2L;
            Review review = Review.of(
                    reviewId, ownerId, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.PENDING, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewWithNickname(reviewId, requesterId))
                    .isInstanceOf(ReviewNotFoundException.class);
        }

        @Test
        @DisplayName("미인증 사용자의 PENDING 후기 조회 시 예외 발생")
        void getReviewWithNickname_unauthenticated_pending_throwsException() {
            // given
            Long reviewId = 1L;
            Long ownerId = 1L;
            Review review = Review.of(
                    reviewId, ownerId, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.PENDING, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));

            // when & then (null requesterId = 미인증)
            assertThatThrownBy(() -> reviewService.getReviewWithNickname(reviewId, null))
                    .isInstanceOf(ReviewNotFoundException.class);
        }

        @Test
        @DisplayName("타인의 REJECTED 후기 조회 시 예외 발생")
        void getReviewWithNickname_otherUser_rejected_throwsException() {
            // given
            Long reviewId = 1L;
            Long ownerId = 1L;
            Long requesterId = 2L;
            Review review = Review.of(
                    reviewId, ownerId, 1L, 1L, "테스트 후기",
                    4.5, ApprovalStatus.REJECTED, false,
                    LocalDateTime.now(), LocalDateTime.now(),
                    createDefaultDetails()
            );

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewWithNickname(reviewId, requesterId))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("강의별 승인된 후기 목록 조회")
    class GetApprovedReviewsTest {

        @Test
        @DisplayName("승인된 후기 목록 조회 성공")
        void getApprovedReviewsByLecture_success() {
            // given
            Long lectureId = 1L;
            List<Review> reviews = List.of(
                    Review.of(1L, 1L, lectureId, 1L, "후기1", 4.5, ApprovalStatus.APPROVED, false, LocalDateTime.now(), LocalDateTime.now(), null),
                    Review.of(2L, 2L, lectureId, 2L, "후기2", 4.0, ApprovalStatus.APPROVED, false, LocalDateTime.now(), LocalDateTime.now(), null)
            );

            given(reviewRepository.findByLectureIdAndApprovalStatus(lectureId, ApprovalStatus.APPROVED))
                    .willReturn(reviews);

            // when
            List<Review> result = reviewService.getApprovedReviewsByLecture(lectureId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getApprovalStatus() == ApprovalStatus.APPROVED);
        }

        @Test
        @DisplayName("승인된 후기가 없으면 빈 리스트 반환")
        void getApprovedReviewsByLecture_empty() {
            // given
            Long lectureId = 1L;

            given(reviewRepository.findByLectureIdAndApprovalStatus(lectureId, ApprovalStatus.APPROVED))
                    .willReturn(List.of());

            // when
            List<Review> result = reviewService.getApprovedReviewsByLecture(lectureId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("기관별 승인된 후기 페이지네이션 조회")
    class GetApprovedReviewsByOrganizationWithPaginationTest {

        @Test
        @DisplayName("기관별 승인된 후기 페이지네이션 조회 성공")
        void getApprovedReviewsByOrganizationWithPagination_success() {
            // given
            Long organizationId = 1L;
            int page = 0;
            int size = 10;
            ReviewSortType sortType = ReviewSortType.LATEST;

            List<Review> reviews = List.of(
                    Review.of(1L, 1L, 1L, 1L, "후기1", 4.5, ApprovalStatus.APPROVED, false,
                            LocalDateTime.now(), LocalDateTime.now(), null),
                    Review.of(2L, 2L, 2L, 2L, "후기2", 4.0, ApprovalStatus.APPROVED, false,
                            LocalDateTime.now(), LocalDateTime.now(), null)
            );
            Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(page, size), 2);

            Member member1 = createMemberWithNickname("사용자1");
            Member member2 = Member.of(2L, "test2@example.com", "pwd", "김철수", "사용자2",
                    "010-1111-2222", Role.USER, null, "부산", LocalDateTime.now(), LocalDateTime.now());

            given(reviewRepository.findByOrganizationIdAndApprovalStatusWithPagination(
                    eq(organizationId), eq(ApprovalStatus.APPROVED), any(Pageable.class)))
                    .willReturn(reviewPage);
            given(memberRepository.findAllByIds(anyList()))
                    .willReturn(List.of(member1, member2));

            // when
            Page<ReviewWithNickname> result = reviewService.getApprovedReviewsByOrganizationWithPagination(
                    organizationId, page, size, sortType);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("후기가 없으면 빈 페이지 반환")
        void getApprovedReviewsByOrganizationWithPagination_empty() {
            // given
            Long organizationId = 1L;
            int page = 0;
            int size = 10;
            ReviewSortType sortType = ReviewSortType.LATEST;

            Page<Review> emptyPage = new PageImpl<>(List.of(), PageRequest.of(page, size), 0);

            given(reviewRepository.findByOrganizationIdAndApprovalStatusWithPagination(
                    eq(organizationId), eq(ApprovalStatus.APPROVED), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<ReviewWithNickname> result = reviewService.getApprovedReviewsByOrganizationWithPagination(
                    organizationId, page, size, sortType);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("별점 높은순 정렬로 조회")
        void getApprovedReviewsByOrganizationWithPagination_sortByScoreDesc() {
            // given
            Long organizationId = 1L;
            int page = 0;
            int size = 10;
            ReviewSortType sortType = ReviewSortType.SCORE_DESC;

            List<Review> reviews = List.of(
                    Review.of(1L, 1L, 1L, 1L, "높은 점수", 5.0, ApprovalStatus.APPROVED, false,
                            LocalDateTime.now(), LocalDateTime.now(), null),
                    Review.of(2L, 2L, 2L, 2L, "낮은 점수", 3.0, ApprovalStatus.APPROVED, false,
                            LocalDateTime.now(), LocalDateTime.now(), null)
            );
            Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(page, size, sortType.getSort()), 2);

            Member member1 = createMemberWithNickname("사용자1");
            Member member2 = Member.of(2L, "test2@example.com", "pwd", "김철수", "사용자2",
                    "010-1111-2222", Role.USER, null, "부산", LocalDateTime.now(), LocalDateTime.now());

            given(reviewRepository.findByOrganizationIdAndApprovalStatusWithPagination(
                    eq(organizationId), eq(ApprovalStatus.APPROVED), any(Pageable.class)))
                    .willReturn(reviewPage);
            given(memberRepository.findAllByIds(anyList()))
                    .willReturn(List.of(member1, member2));

            // when
            Page<ReviewWithNickname> result = reviewService.getApprovedReviewsByOrganizationWithPagination(
                    organizationId, page, size, sortType);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).review().getScore()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("두번째 페이지 조회")
        void getApprovedReviewsByOrganizationWithPagination_secondPage() {
            // given
            Long organizationId = 1L;
            int page = 1;
            int size = 10;
            ReviewSortType sortType = ReviewSortType.LATEST;

            List<Review> reviews = List.of(
                    Review.of(11L, 11L, 11L, 11L, "후기11", 4.0, ApprovalStatus.APPROVED, false,
                            LocalDateTime.now(), LocalDateTime.now(), null)
            );
            Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(page, size), 11);

            Member member = Member.of(11L, "test11@example.com", "pwd", "이영희", "사용자11",
                    "010-1111-1111", Role.USER, null, "대전", LocalDateTime.now(), LocalDateTime.now());

            given(reviewRepository.findByOrganizationIdAndApprovalStatusWithPagination(
                    eq(organizationId), eq(ApprovalStatus.APPROVED), any(Pageable.class)))
                    .willReturn(reviewPage);
            given(memberRepository.findAllByIds(anyList()))
                    .willReturn(List.of(member));

            // when
            Page<ReviewWithNickname> result = reviewService.getApprovedReviewsByOrganizationWithPagination(
                    organizationId, page, size, sortType);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(11);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }

    // 헬퍼 메서드
    private Member createMemberWithNickname(String nickname) {
        return Member.of(
                1L,
                "test@example.com",
                "encodedPassword",
                "홍길동",
                nickname,
                "010-1234-5678",
                Role.USER,
                null,
                "서울",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

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
