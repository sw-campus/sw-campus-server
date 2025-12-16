package com.swcampus.infra.postgres.review;

import com.swcampus.domain.review.ApprovalStatus;
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
}
