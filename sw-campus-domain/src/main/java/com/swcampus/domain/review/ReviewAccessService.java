package com.swcampus.domain.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.review.dto.ReviewBlindStatus;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyRepository;

import lombok.RequiredArgsConstructor;

/**
 * 리뷰 접근 권한(블라인드 해제 여부)을 관리하는 서비스.
 * 
 * 블라인드 해제 조건 (OR):
 * - 설문조사 100% 완료 (MemberSurvey.completedAt != null)
 * - 승인된 리뷰 1개 이상 보유 (ApprovalStatus.APPROVED)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewAccessService {

    private final ReviewRepository reviewRepository;
    private final MemberSurveyRepository surveyRepository;

    /**
     * 특정 회원의 리뷰 블라인드 해제 여부를 확인합니다.
     * 
     * @param memberId 회원 ID (null이면 비회원으로 간주)
     * @return 블라인드 해제 여부
     */
    public boolean isReviewUnblinded(Long memberId) {
        if (memberId == null) {
            return false;
        }

        // 조건 1: 설문조사 100% 완료
        boolean hasSurveyCompleted = surveyRepository.findByMemberId(memberId)
            .map(MemberSurvey::isComplete)
            .orElse(false);
        if (hasSurveyCompleted) {
            return true;
        }

        // 조건 2: 승인된 리뷰 존재
        return reviewRepository.existsByMemberIdAndApprovalStatus(
            memberId, ApprovalStatus.APPROVED);
    }

    /**
     * 특정 회원의 블라인드 상태 상세 정보를 조회합니다.
     * 
     * @param memberId 회원 ID
     * @return 블라인드 상태 (해제 여부, 리뷰 보유 여부, 설문 완료 여부)
     */
    public ReviewBlindStatus getBlindStatus(Long memberId) {
        boolean hasApprovedReview = reviewRepository.existsByMemberIdAndApprovalStatus(
            memberId, ApprovalStatus.APPROVED);
        
        boolean hasSurveyCompleted = surveyRepository.findByMemberId(memberId)
            .map(MemberSurvey::isComplete)
            .orElse(false);

        return ReviewBlindStatus.of(hasApprovedReview, hasSurveyCompleted);
    }
}
