package com.swcampus.domain.review;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.certificate.exception.CertificateNotFoundException;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.review.dto.PendingReviewInfo;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final CertificateRepository certificateRepository;
    private final MemberRepository memberRepository;
    private final LectureService lectureService;
    private final EmailService emailService;

    /**
     * 대기 중인 후기 목록 조회 (수료증 또는 후기가 PENDING)
     * 강의명, 회원정보, 수료증 상태를 배치 조회하여 N+1 문제 방지
     */
    public List<PendingReviewInfo> getPendingReviewsWithDetails() {
        List<Review> reviews = reviewRepository.findPendingReviews();
        return enrichReviewsWithDetails(reviews);
    }

    /**
     * 후기 상세 조회 (강의명, 회원정보, 수료증 상태 포함)
     */
    public PendingReviewInfo getReviewWithDetails(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(ReviewNotFoundException::new);

        String lectureName = lectureService.getLectureNames(List.of(review.getLectureId()))
            .getOrDefault(review.getLectureId(), "알 수 없음");

        Member member = memberRepository.findById(review.getMemberId()).orElse(null);
        String userName = member != null ? member.getName() : "알 수 없음";
        String nickname = member != null ? member.getNickname() : "알 수 없음";

        Certificate certificate = certificateRepository.findById(review.getCertificateId()).orElse(null);
        ApprovalStatus certStatus = certificate != null ? certificate.getApprovalStatus() : ApprovalStatus.PENDING;

        return PendingReviewInfo.of(review, lectureName, userName, nickname, certStatus);
    }

    /**
     * 수료증 조회 (1단계 모달용)
     */
    public Certificate getCertificate(Long certificateId) {
        return certificateRepository.findById(certificateId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));
    }

    /**
     * 수료증 승인 (1단계)
     */
    @Transactional
    public Certificate approveCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));

        certificate.approve();
        return certificateRepository.save(certificate);
    }

    /**
     * 수료증 반려 (1단계)
     * - 수료증만 REJECTED
     * - 반려 이메일 발송
     * - 2단계 진행 안 함
     */
    @Transactional
    public Certificate rejectCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));

        certificate.reject();
        Certificate saved = certificateRepository.save(certificate);

        // 반려 이메일 발송
        Member member = memberRepository.findById(certificate.getMemberId())
                .orElse(null);
        if (member != null) {
            emailService.sendCertificateRejectionEmail(member.getEmail());
        }

        return saved;
    }

    /**
     * 후기 상세 조회 (2단계 모달용)
     */
    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);
    }

    /**
     * 후기 승인 (2단계)
     */
    @Transactional
    public Review approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        review.approve();
        return reviewRepository.save(review);
    }

    /**
     * 후기 반려 (2단계)
     * - 후기만 REJECTED (수료증은 이미 승인 상태)
     * - 반려 이메일 발송
     */
    @Transactional
    public Review rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        review.reject();
        Review saved = reviewRepository.save(review);

        // 반려 이메일 발송
        Member member = memberRepository.findById(review.getMemberId())
                .orElse(null);
        if (member != null) {
            emailService.sendReviewRejectionEmail(member.getEmail());
        }

        return saved;
    }

    /**
     * 후기 블라인드 처리
     */
    @Transactional
    public Review blindReview(Long reviewId, boolean blurred) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        if (blurred) {
            review.blind();
        } else {
            review.unblind();
        }

        return reviewRepository.save(review);
    }

    /**
     * 전체 후기 목록 조회 (필터링 및 페이지네이션)
     * 강의명, 회원정보, 수료증 상태를 배치 조회하여 N+1 문제 방지
     */
    public Page<PendingReviewInfo> getAllReviewsWithDetails(ApprovalStatus status, String keyword, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAllWithDetails(status, keyword, pageable);

        if (reviewPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Review> reviews = reviewPage.getContent();
        ReviewEnrichmentData enrichmentData = fetchEnrichmentData(reviews);

        return reviewPage.map(review -> toReviewInfo(review, enrichmentData));
    }

    /**
     * 후기 목록을 PendingReviewInfo로 변환 (배치 조회로 N+1 방지)
     */
    private List<PendingReviewInfo> enrichReviewsWithDetails(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }

        ReviewEnrichmentData enrichmentData = fetchEnrichmentData(reviews);

        return reviews.stream()
            .map(review -> toReviewInfo(review, enrichmentData))
            .toList();
    }

    /**
     * 후기 목록에 필요한 연관 데이터를 배치 조회
     */
    private ReviewEnrichmentData fetchEnrichmentData(List<Review> reviews) {
        List<Long> lectureIds = reviews.stream()
            .map(Review::getLectureId)
            .distinct()
            .toList();
        Map<Long, String> lectureNames = lectureService.getLectureNames(lectureIds);

        List<Long> memberIds = reviews.stream()
            .map(Review::getMemberId)
            .distinct()
            .toList();
        Map<Long, Member> memberMap = memberRepository.findAllByIds(memberIds).stream()
            .collect(Collectors.toMap(Member::getId, m -> m));

        List<Long> certificateIds = reviews.stream()
            .map(Review::getCertificateId)
            .distinct()
            .toList();
        Map<Long, Certificate> certificateMap = certificateRepository.findAllByIds(certificateIds);

        return new ReviewEnrichmentData(lectureNames, memberMap, certificateMap);
    }

    /**
     * Review를 PendingReviewInfo로 변환
     */
    private PendingReviewInfo toReviewInfo(Review review, ReviewEnrichmentData data) {
        String lectureName = data.lectureNames().getOrDefault(review.getLectureId(), "알 수 없음");
        Member member = data.memberMap().get(review.getMemberId());
        String userName = member != null ? member.getName() : "알 수 없음";
        String nickname = member != null ? member.getNickname() : "알 수 없음";
        Certificate certificate = data.certificateMap().get(review.getCertificateId());
        ApprovalStatus certStatus = certificate != null ? certificate.getApprovalStatus() : ApprovalStatus.PENDING;

        return PendingReviewInfo.of(review, lectureName, userName, nickname, certStatus);
    }

    /**
     * 후기 보강에 필요한 배치 조회 데이터
     */
    private record ReviewEnrichmentData(
        Map<Long, String> lectureNames,
        Map<Long, Member> memberMap,
        Map<Long, Certificate> certificateMap
    ) {}
}
