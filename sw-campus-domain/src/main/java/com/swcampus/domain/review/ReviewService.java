package com.swcampus.domain.review;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.certificate.exception.CertificateNotVerifiedException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.review.exception.ReviewAlreadyExistsException;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import com.swcampus.domain.review.exception.ReviewNotModifiableException;
import com.swcampus.domain.review.exception.ReviewNotOwnerException;
import lombok.RequiredArgsConstructor;
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
     * 반려된 후기만 수정할 수 있습니다.
     */
    @Transactional
    public Review updateReview(Long memberId, Long reviewId, 
                                String comment, List<ReviewDetail> details) {
        // 1. 후기 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        // 2. 작성자 확인
        if (!review.getMemberId().equals(memberId)) {
            throw new ReviewNotOwnerException();
        }

        // 3. 승인 상태 확인 (반려된 후기만 수정 가능)
        if (review.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new ReviewNotModifiableException();
        }

        // 4. 후기 수정
        review.update(comment, details);
        review.resubmit();
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
     */
    public ReviewWithNickname getReviewWithNickname(Long reviewId) {
        Review review = getReview(reviewId);
        String nickname = memberRepository.findById(review.getMemberId())
                .map(Member::getNickname)
                .orElse(null);
        return ReviewWithNickname.of(review, nickname);
    }

    /**
     * 회원 닉네임 조회
     */
    public String getNickname(Long memberId) {
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
     * 내가 작성한 후기 조회 (lectureId 기준)
     */
    public Review getMyReviewByLecture(Long memberId, Long lectureId) {
        return reviewRepository.findByMemberIdAndLectureId(memberId, lectureId)
                .orElseThrow(ReviewNotFoundException::new);
    }

    /**
     * 리뷰 목록에 닉네임을 배치로 조회하여 매핑
     */
    private List<ReviewWithNickname> toReviewsWithNicknames(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = reviews.stream()
                .map(Review::getMemberId)
                .distinct()
                .toList();

        Map<Long, String> nicknameMap = memberRepository.findAllByIds(memberIds).stream()
                .collect(Collectors.toMap(
                        Member::getId,
                        member -> member.getNickname() != null ? member.getNickname() : "",
                        (existing, replacement) -> existing
                ));

        return reviews.stream()
                .map(review -> ReviewWithNickname.of(
                        review,
                        nicknameMap.get(review.getMemberId())
                ))
                .toList();
    }
}

