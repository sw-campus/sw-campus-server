package com.swcampus.infra.postgres.review;

import com.swcampus.domain.common.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {

        boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId);

        @Query("SELECT r FROM ReviewEntity r LEFT JOIN FETCH r.details WHERE r.id = :id")
        Optional<ReviewEntity> findByIdWithDetails(@Param("id") Long id);

        @Query("SELECT r FROM ReviewEntity r LEFT JOIN FETCH r.details WHERE r.memberId = :memberId AND r.lectureId = :lectureId")
        Optional<ReviewEntity> findByMemberIdAndLectureIdWithDetails(@Param("memberId") Long memberId,
                        @Param("lectureId") Long lectureId);

        @Query("SELECT r FROM ReviewEntity r LEFT JOIN FETCH r.details WHERE r.lectureId = :lectureId AND r.approvalStatus = :status")
        List<ReviewEntity> findByLectureIdAndApprovalStatusWithDetails(@Param("lectureId") Long lectureId,
                        @Param("status") ApprovalStatus status);

        @Query("SELECT r FROM ReviewEntity r LEFT JOIN FETCH r.details WHERE EXISTS (SELECT 1 FROM LectureEntity l WHERE l.lectureId = r.lectureId AND l.orgId = :organizationId) AND r.approvalStatus = :status")
        List<ReviewEntity> findByOrganizationIdAndApprovalStatusWithDetails(
                        @Param("organizationId") Long organizationId,
                        @Param("status") ApprovalStatus status);

        @Query("SELECT r FROM ReviewEntity r LEFT JOIN FETCH r.details WHERE r.approvalStatus = :status")
        List<ReviewEntity> findByApprovalStatusWithDetails(@Param("status") ApprovalStatus status);

        @Query("SELECT r FROM ReviewEntity r LEFT JOIN FETCH r.details WHERE r.memberId = :memberId")
        List<ReviewEntity> findAllByMemberIdWithDetails(@Param("memberId") Long memberId);

        @Query("SELECT AVG(r.score) FROM ReviewEntity r WHERE r.lectureId = :lectureId AND r.approvalStatus = :status")
        Double findAverageScoreByLectureId(@Param("lectureId") Long lectureId, @Param("status") ApprovalStatus status);

        @Query("SELECT r.lectureId, AVG(r.score) FROM ReviewEntity r " +
                        "WHERE r.lectureId IN :lectureIds AND r.approvalStatus = :status " +
                        "GROUP BY r.lectureId")
        List<Object[]> findAverageScoresByLectureIds(@Param("lectureIds") List<Long> lectureIds,
                        @Param("status") ApprovalStatus status);

        @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.lectureId = :lectureId AND r.approvalStatus = :status")
        Long countReviewsByLectureId(@Param("lectureId") Long lectureId, @Param("status") ApprovalStatus status);

        @Query("SELECT r.lectureId, COUNT(r) FROM ReviewEntity r " +
                        "WHERE r.lectureId IN :lectureIds AND r.approvalStatus = :status " +
                        "GROUP BY r.lectureId")
        List<Object[]> countReviewsByLectureIds(@Param("lectureIds") List<Long> lectureIds,
                        @Param("status") ApprovalStatus status);

        /**
         * 여러 강의의 리뷰 통계(평균점수, 리뷰수)를 한 번에 조회 (2 쿼리 → 1 쿼리 최적화)
         * 반환: List of Object[] { lectureId, avgScore, reviewCount }
         */
        @Query("SELECT r.lectureId, COALESCE(AVG(r.score), 0.0), COUNT(r) " +
                        "FROM ReviewEntity r " +
                        "WHERE r.lectureId IN :lectureIds AND r.approvalStatus = :status " +
                        "GROUP BY r.lectureId")
        List<Object[]> findReviewStatsByLectureIds(@Param("lectureIds") List<Long> lectureIds,
                        @Param("status") ApprovalStatus status);

        @Query(value = "SELECT DISTINCT r FROM ReviewEntity r " +
                        "LEFT JOIN FETCH r.details " +
                        "LEFT JOIN LectureEntity l ON l.lectureId = r.lectureId " +
                        "WHERE (:status IS NULL OR r.approvalStatus = :status) " +
                        "AND (:keyword IS NULL OR :keyword = '' OR l.lectureName ILIKE CONCAT('%', :keyword, '%'))", countQuery = "SELECT COUNT(DISTINCT r) FROM ReviewEntity r "
                                        +
                                        "LEFT JOIN LectureEntity l ON l.lectureId = r.lectureId " +
                                        "WHERE (:status IS NULL OR r.approvalStatus = :status) " +
                                        "AND (:keyword IS NULL OR :keyword = '' OR l.lectureName ILIKE CONCAT('%', :keyword, '%'))")
        Page<ReviewEntity> findAllWithDetailsAndKeyword(
                        @Param("status") ApprovalStatus status,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT r FROM ReviewEntity r " +
                        "LEFT JOIN FETCH r.details " +
                        "WHERE EXISTS (SELECT 1 FROM LectureEntity l WHERE l.lectureId = r.lectureId AND l.orgId = :organizationId) "
                        +
                        "AND r.approvalStatus = :status", countQuery = "SELECT COUNT(DISTINCT r) FROM ReviewEntity r " +
                                        "WHERE EXISTS (SELECT 1 FROM LectureEntity l WHERE l.lectureId = r.lectureId AND l.orgId = :organizationId) "
                                        +
                                        "AND r.approvalStatus = :status")
        Page<ReviewEntity> findByOrganizationIdAndApprovalStatusWithPagination(
                        @Param("organizationId") Long organizationId,
                        @Param("status") ApprovalStatus status,
                        Pageable pageable);

        long countByApprovalStatus(ApprovalStatus status);

        /**
         * 수료증이 승인된 리뷰 수를 조회합니다.
         */
        @Query("SELECT COUNT(r) FROM ReviewEntity r " +
                        "JOIN CertificateEntity c ON c.id = r.certificateId " +
                        "WHERE c.approvalStatus = com.swcampus.domain.common.ApprovalStatus.APPROVED")
        long countWithApprovedCertificate();

        /**
         * 수료증이 승인되고 특정 리뷰 상태인 리뷰 수를 조회합니다.
         */
        @Query("SELECT COUNT(r) FROM ReviewEntity r " +
                        "JOIN CertificateEntity c ON c.id = r.certificateId " +
                        "WHERE c.approvalStatus = com.swcampus.domain.common.ApprovalStatus.APPROVED " +
                        "AND r.approvalStatus = :reviewStatus")
        long countWithApprovedCertificateAndReviewStatus(@Param("reviewStatus") ApprovalStatus reviewStatus);
}
