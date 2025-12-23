package com.swcampus.domain.lecture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSortType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLectureService {

    private final LectureRepository lectureRepository;

    /**
     * 관리자용 강의 목록을 조회합니다. 승인 상태와 키워드로 필터링할 수 있습니다.
     */
    public Page<Lecture> searchLectures(LectureAuthStatus status, String keyword, Pageable pageable) {
        LectureSearchCondition condition = LectureSearchCondition.builder()
                .lectureAuthStatus(status)
                .text(keyword)
                .sort(LectureSortType.LATEST)
                .pageable(pageable)
                .build();
        return lectureRepository.searchLectures(condition);
    }

    /**
     * 강의 상세 정보를 조회합니다.
     */
    public Lecture getLectureDetail(Long id) {
        return lectureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + id));
    }

    /**
     * 강의를 승인합니다.
     */
    @Transactional
    public Lecture approveLecture(Long id) {
        return lectureRepository.updateAuthStatus(id, LectureAuthStatus.APPROVED);
    }

    /**
     * 강의를 반려합니다.
     */
    @Transactional
    public Lecture rejectLecture(Long id) {
        return lectureRepository.updateAuthStatus(id, LectureAuthStatus.REJECTED);
    }

    /**
     * 강의 상태별 통계를 조회합니다.
     */
    public LectureApprovalStats getStats() {
        long total = lectureRepository.countAll();
        long pending = lectureRepository.countByAuthStatus(LectureAuthStatus.PENDING);
        long approved = lectureRepository.countByAuthStatus(LectureAuthStatus.APPROVED);
        long rejected = lectureRepository.countByAuthStatus(LectureAuthStatus.REJECTED);
        return new LectureApprovalStats(total, pending, approved, rejected);
    }

    public record LectureApprovalStats(long total, long pending, long approved, long rejected) {}
}
