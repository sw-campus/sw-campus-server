package com.swcampus.domain.lecture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.swcampus.domain.lecture.dto.LectureSearchCondition;

public interface LectureRepository {
    Lecture save(Lecture lecture);

    void saveAll(List<Lecture> lectures);

    Optional<Lecture> findById(Long id);

    List<Lecture> findAllByOrgId(Long orgId);

    Map<Long, String> findLectureNamesByIds(List<Long> lectureIds);

    Page<Lecture> searchLectures(LectureSearchCondition condition);

    List<Lecture> findAllExpiredAndRecruiting(LocalDateTime now);

    List<Lecture> findAllByOrgIdAndLectureAuthStatus(Long orgId, LectureAuthStatus status);

    Map<Long, Long> countLecturesByStatusAndAuthStatusAndOrgIdIn(LectureStatus status, LectureAuthStatus authStatus,
            List<Long> orgIds);

    List<Lecture> findAllByIds(List<Long> lectureIds);

    /**
     * 리뷰 통계 없이 강의 목록 조회 (장바구니 등 리뷰 불필요한 경우 사용)
     */
    List<Lecture> findAllByIdsWithoutReviewStats(List<Long> lectureIds);

    /**
     * 강의의 승인 상태만 업데이트합니다.
     * 자식 컬렉션(quals, steps, adds 등)은 건드리지 않습니다.
     */
    Lecture updateAuthStatus(Long lectureId, LectureAuthStatus status);

    // Statistics methods
    long countAll();
    long countByAuthStatus(LectureAuthStatus status);

    void deleteById(Long id);
}