package com.swcampus.domain.mypage;

import com.swcampus.domain.certificate.Certificate;
import com.swcampus.domain.certificate.CertificateService;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.mypage.dto.CompletedLectureInfo;
import com.swcampus.domain.mypage.dto.MyReviewInfo;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.review.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 비즈니스 로직 서비스
 * 여러 도메인(Certificate, Lecture, Review, Organization)을 조합하여 마이페이지 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final CertificateService certificateService;
    private final LectureService lectureService;
    private final ReviewService reviewService;
    private final OrganizationService organizationService;

    /**
     * 수강 완료 강의 목록 조회 (수료증 인증이 승인된 강의)
     *
     * @param memberId 회원 ID
     * @return 수강 완료 강의 목록
     */
    public List<CompletedLectureInfo> getCompletedLectures(Long memberId) {
        // 승인된 수료증 목록 조회
        List<Certificate> certificates = certificateService.findAllByMemberId(memberId);

        if (certificates.isEmpty()) {
            return List.of();
        }

        // 강의 정보 조회 (N+1 방지를 위해 IN 절 사용)
        List<Long> lectureIds = certificates.stream()
            .map(Certificate::getLectureId)
            .toList();
        Map<Long, Lecture> lectureMap = lectureService.getLecturesByIds(lectureIds);

        // 기관명 조회를 위한 orgId 수집
        List<Long> orgIds = lectureMap.values().stream()
            .map(Lecture::getOrgId)
            .distinct()
            .toList();
        Map<Long, String> orgNames = organizationService.getOrganizationNames(orgIds);

        // 후기 작성 여부 확인
        List<Review> reviews = reviewService.findAllByMemberId(memberId);
        Set<Long> reviewedLectureIds = reviews.stream()
            .map(Review::getLectureId)
            .collect(Collectors.toSet());

        // 응답 생성
        return certificates.stream()
            .filter(cert -> lectureMap.containsKey(cert.getLectureId()))
            .map(cert -> {
                Lecture lecture = lectureMap.get(cert.getLectureId());
                String orgName = orgNames.getOrDefault(lecture.getOrgId(), "Unknown");
                boolean hasReview = reviewedLectureIds.contains(cert.getLectureId());
                return new CompletedLectureInfo(
                    cert.getId(),
                    lecture.getLectureId(),
                    lecture.getLectureName(),
                    lecture.getLectureImageUrl(),
                    orgName,
                    cert.getCreatedAt(),
                    !hasReview  // 후기가 없으면 작성 가능
                );
            })
            .toList();
    }

    /**
     * 내 후기 목록 조회
     *
     * @param memberId 회원 ID
     * @return 내 후기 목록
     */
    public List<MyReviewInfo> getMyReviews(Long memberId) {
        List<Review> reviews = reviewService.findAllByMemberId(memberId);
        List<Long> lectureIds = reviews.stream()
            .map(Review::getLectureId)
            .toList();

        // Note: getLectureNames uses 'IN' clause, so N+1 problem is avoided here.
        Map<Long, String> lectureNames = lectureService.getLectureNames(lectureIds);

        return reviews.stream()
            .map(review -> new MyReviewInfo(
                review.getId(),
                review.getLectureId(),
                lectureNames.getOrDefault(review.getLectureId(), "Unknown"),
                review.getScore(),
                review.getComment(),
                review.getApprovalStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getApprovalStatus() == ApprovalStatus.REJECTED  // REJECTED만 수정 가능
            ))
            .toList();
    }
}
