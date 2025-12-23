package com.swcampus.infra.postgres.lecture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureStatus;

public interface LectureJpaRepository extends JpaRepository<LectureEntity, Long> {
        List<LectureEntity> findAllByDeadlineBeforeAndStatus(LocalDateTime now, LectureStatus status);

        /**
         * 기관별 강의 조회 시 Curriculum → Category까지 함께 fetch
         */
        @Query("SELECT DISTINCT l FROM LectureEntity l " +
                        "LEFT JOIN FETCH l.lectureCurriculums lc " +
                        "LEFT JOIN FETCH lc.curriculum c " +
                        "LEFT JOIN FETCH c.category " +
                        "WHERE l.orgId = :orgId")
        List<LectureEntity> findAllByOrgIdWithCurriculums(@Param("orgId") Long orgId);

        /**
         * 기관별 강의 조회 시 - 승인 상태 필터링 포함
         */
        @Query("SELECT DISTINCT l FROM LectureEntity l " +
                        "LEFT JOIN FETCH l.lectureCurriculums lc " +
                        "LEFT JOIN FETCH lc.curriculum c " +
                        "LEFT JOIN FETCH c.category " +
                        "WHERE l.orgId = :orgId AND l.lectureAuthStatus = :authStatus")
        List<LectureEntity> findAllByOrgIdAndLectureAuthStatusWithCurriculums(@Param("orgId") Long orgId,
                        @Param("authStatus") LectureAuthStatus authStatus);

        @Query("SELECT l.orgId, COUNT(l) FROM LectureEntity l WHERE l.status = :status AND l.lectureAuthStatus = :authStatus AND l.orgId IN :orgIds GROUP BY l.orgId")
        List<Object[]> countByStatusAndOrgIdInGroupByOrgId(@Param("status") LectureStatus status,
                        @Param("authStatus") LectureAuthStatus authStatus,
                        @Param("orgIds") List<Long> orgIds);

        @Query("SELECT l.lectureId, l.lectureName FROM LectureEntity l WHERE l.lectureId IN :ids")
        List<Object[]> findIdAndNameByIdIn(@Param("ids") List<Long> ids);

        /**
         * Lecture 상세 조회 시 Curriculum → Category까지 함께 fetch
         * (기존 관계들은 LAZY 로딩으로 트랜잭션 내에서 정상 동작)
         */
        @Query("SELECT DISTINCT l FROM LectureEntity l " +
                        "LEFT JOIN FETCH l.lectureCurriculums lc " +
                        "LEFT JOIN FETCH lc.curriculum c " +
                        "LEFT JOIN FETCH c.category " +
                        "WHERE l.lectureId = :id")
        Optional<LectureEntity> findByIdWithCategory(@Param("id") Long id);

        /**
         * 여러 Lecture를 ID 목록으로 조회 시 Curriculum → Category까지 함께 fetch
         */
        @Query("SELECT DISTINCT l FROM LectureEntity l " +
                        "LEFT JOIN FETCH l.lectureCurriculums lc " +
                        "LEFT JOIN FETCH lc.curriculum c " +
                        "LEFT JOIN FETCH c.category " +
                        "WHERE l.lectureId IN :ids")
        List<LectureEntity> findAllByIdInWithCurriculums(@Param("ids") List<Long> ids);

        long countByLectureAuthStatus(LectureAuthStatus status);
}
