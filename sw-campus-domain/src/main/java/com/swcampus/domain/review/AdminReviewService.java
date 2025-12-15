package com.swcampus.domain.review;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.certificate.exception.CertificateNotFoundException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final CertificateRepository certificateRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;

    /**
     * 대기 중인 후기 목록 조회 (수료증 또는 후기가 PENDING)
     */
    public List<Review> getPendingReviews() {
        return reviewRepository.findPendingReviews();
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
}
