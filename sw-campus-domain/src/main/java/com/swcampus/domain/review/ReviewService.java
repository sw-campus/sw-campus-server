package com.swcampus.domain.review;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.certificate.exception.CertificateNotVerifiedException;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.review.exception.ReviewAlreadyExistsException;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import com.swcampus.domain.review.exception.ReviewNotModifiableException;
import com.swcampus.domain.review.exception.ReviewNotOwnerException;
import com.swcampus.domain.review.dto.ReviewListResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CertificateRepository certificateRepository;
    private final MemberRepository memberRepository;
    private final ReviewAccessService reviewAccessService;

    /**
     * 후기 작성 가능 여부 확인
     */
    public ReviewEligibility checkEligibility(Long memberId, Long lectureId) {
        // 1. 회원 정보 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // 2. 닉네임 설정 여부
        boolean hasNickname = member.getNickname() != null && 
                              !member.getNickname().isBlank();

        // 3. 수료증 인증 여부
        boolean hasCertificate = certificateRepository
                .existsByMemberIdAndLectureId(memberId, lectureId);

        // 4. 기존 후기 존재 여부 (없어야 작성 가능)
        boolean canWrite = !reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId);

        return ReviewEligibility.of(hasNickname, hasCertificate, canWrite);
    }

    /**
     * 후기 작성
     */
    @Transactional
    public Review createReview(Long memberId, Long lectureId, 
                                String comment, List<ReviewDetail> details) {
        // 1. 수료증 인증 확인
        Certificate certificate = certificateRepository
                .findByMemberIdAndLectureId(memberId, lectureId)
                .orElseThrow(CertificateNotVerifiedException::new);

        // 2. 중복 후기 확인
        if (reviewRepository.existsByMemberIdAndLectureId(memberId, lectureId)) {
            throw new ReviewAlreadyExistsException();
        }

        // 3. 후기 생성
        Review review = Review.create(
                memberId, 
                lectureId, 
                certificate.getId(), 
                comment, 
                details
        );

        return reviewRepository.save(review);
    }

    /**
     * 후기 수정
     * 승인된 후기는 수정할 수 없습니다.
     */
    @Transactional
    public Review updateReview(Long memberId, Long reviewId,
                                String comment, List<ReviewDetail> details) {
        // 1. 후기 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        // 2. 작성자 확인 (탈퇴한 회원의 후기는 수정 불가)
        if (review.getMemberId() == null || !review.getMemberId().equals(memberId)) {
            throw new ReviewNotOwnerException();
        }

        // 3. 승인 상태 확인 (승인된 후기만 수정 불가)
        if (review.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new ReviewNotModifiableException();
        }

        // 4. 후기 수정
        review.update(comment, details);
        if (review.getApprovalStatus() == ApprovalStatus.REJECTED) {
            review.resubmit();
        }
        return reviewRepository.save(review);
    }

    public List<Review> findAllByMemberId(Long memberId) {
        return reviewRepository.findAllByMemberId(memberId);
    }

    /**
     * 후기 상세 조회
     */
    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);
    }

    /**
     * 후기 상세 조회 (닉네임 포함)
     *
     * @param reviewId 후기 ID
     * @param requesterId 요청자 ID (null이면 미인증 사용자)
     * @return 후기 정보
     * @throws ReviewNotFoundException 후기가 없거나 접근 권한이 없는 경우
     */
    public ReviewWithNickname getReviewWithNickname(Long reviewId, Long requesterId) {
        Review review = getReview(reviewId);

        // 접근 권한 확인: 본인 후기이거나 승인된 후기만 조회 가능
        boolean isOwner = requesterId != null && requesterId.equals(review.getMemberId());
        boolean isApproved = review.getApprovalStatus() == ApprovalStatus.APPROVED;

        if (!isOwner && !isApproved) {
            throw new ReviewNotFoundException();
        }

        String nickname = review.getMemberId() == null
                ? null
                : memberRepository.findById(review.getMemberId())
                        .map(Member::getNickname)
                        .orElse(null);
        return ReviewWithNickname.of(review, nickname);
    }

    /**
     * 회원 닉네임 조회
     * 탈퇴한 회원(memberId가 NULL)의 경우 null을 반환합니다.
     */
    public String getNickname(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return memberRepository.findById(memberId)
                .map(Member::getNickname)
                .orElse(null);
    }

    /**
     * 강의별 승인된 후기 목록 조회
     */
    public List<Review> getApprovedReviewsByLecture(Long lectureId) {
        return reviewRepository.findByLectureIdAndApprovalStatus(
                lectureId, ApprovalStatus.APPROVED
        );
    }

    /**
     * 강의별 승인된 후기 목록 조회 (닉네임 포함)
     */
    public List<ReviewWithNickname> getApprovedReviewsWithNicknameByLecture(Long lectureId) {
        List<Review> reviews = getApprovedReviewsByLecture(lectureId);
        return toReviewsWithNicknames(reviews);
    }


    /**
     * 강의별 승인된 후기 목록 조회 (블라인드 필터링 적용)
     * 
     * @param lectureId 강의 ID
     * @param requesterId 요청자 ID (null이면 비회원)
     * @return 블라인드 필터링이 적용된 리뷰 목록 (totalCount, isUnblinded 포함)
     */
    public ReviewListResult getApprovedReviewsWithBlind(Long lectureId, Long requesterId) {
        List<Review> allReviews = getApprovedReviewsByLecture(lectureId);
        boolean isUnblinded = reviewAccessService.isReviewUnblinded(requesterId);

        List<Review> visibleReviews = isUnblinded
            ? allReviews
            : allReviews.stream().limit(1).toList();

        return ReviewListResult.of(
            toReviewsWithNicknames(visibleReviews),
            allReviews.size(),
            isUnblinded
        );
    }

    /**
     * 기관별 승인된 후기 목록 조회
     */
    public List<Review> getApprovedReviewsByOrganization(Long organizationId) {
        return reviewRepository.findByOrganizationIdAndApprovalStatus(
                organizationId, ApprovalStatus.APPROVED
        );
    }

    /**
     * 기관별 승인된 후기 목록 조회 (닉네임 포함)
     */
    public List<ReviewWithNickname> getApprovedReviewsWithNicknameByOrganization(Long organizationId) {
        List<Review> reviews = getApprovedReviewsByOrganization(organizationId);
        return toReviewsWithNicknames(reviews);
    }

    /**
     * 기관별 승인된 후기 목록 조회 (페이지네이션, 정렬)
     */
    public Page<ReviewWithNickname> getApprovedReviewsByOrganizationWithPagination(
            Long organizationId, int page, int size, ReviewSortType sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.getSort());
        Page<Review> reviewPage = reviewRepository.findByOrganizationIdAndApprovalStatusWithPagination(
                organizationId, ApprovalStatus.APPROVED, pageable);
        return toPageReviewsWithNicknames(reviewPage);
    }

    private Page<ReviewWithNickname> toPageReviewsWithNicknames(Page<Review> reviewPage) {
        List<Review> reviews = reviewPage.getContent();
        if (reviews.isEmpty()) {
            return reviewPage.map(review -> ReviewWithNickname.of(review, null));
        }

        List<Long> memberIds = reviews.stream()
                .map(Review::getMemberId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> nicknameMap = memberIds.isEmpty()
                ? Map.of()
                : memberRepository.findAllByIds(memberIds).stream()
                        .collect(Collectors.toMap(
                                Member::getId,
                                member -> member.getNickname() != null ? member.getNickname() : "",
                                (existing, replacement) -> existing
                        ));

        return reviewPage.map(review -> ReviewWithNickname.of(
                review,
                review.getMemberId() == null ? null : nicknameMap.get(review.getMemberId())
        ));
    }

    /**
     * 내가 작성한 후기 조회 (lectureId 기준)
     */
    public Review getMyReviewByLecture(Long memberId, Long lectureId) {
        return reviewRepository.findByMemberIdAndLectureId(memberId, lectureId)
                .orElseThrow(ReviewNotFoundException::new);
    }

    /**
     * 내가 작성한 후기 조회 (lectureId 기준, 닉네임 포함)
     */
    public ReviewWithNickname getMyReviewWithNicknameByLecture(Long memberId, Long lectureId) {
        Review review = getMyReviewByLecture(memberId, lectureId);
        String nickname = getNickname(memberId);
        return ReviewWithNickname.of(review, nickname);
    }

    /**
     * 리뷰 목록에 닉네임을 배치로 조회하여 매핑
     * 탈퇴한 회원(memberId가 NULL)의 경우 닉네임이 null로 설정됩니다.
     */
    private List<ReviewWithNickname> toReviewsWithNicknames(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = reviews.stream()
                .map(Review::getMemberId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> nicknameMap = memberIds.isEmpty()
                ? Map.of()
                : memberRepository.findAllByIds(memberIds).stream()
                        .collect(Collectors.toMap(
                                Member::getId,
                                member -> member.getNickname() != null ? member.getNickname() : "",
                                (existing, replacement) -> existing
                        ));

        return reviews.stream()
                .map(review -> ReviewWithNickname.of(
                        review,
                        review.getMemberId() == null ? null : nicknameMap.get(review.getMemberId())
                ))
                .toList();
    }
}

