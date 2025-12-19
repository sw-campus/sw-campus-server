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
        Lecture lecture = getLectureDetail(id);
        Lecture approvedLecture = lecture.approve();
        return lectureRepository.save(approvedLecture);
    }

    /**
     * 강의를 반려합니다.
     */
    @Transactional
    public Lecture rejectLecture(Long id) {
        Lecture lecture = getLectureDetail(id);
        Lecture rejectedLecture = lecture.reject();
        return lectureRepository.save(rejectedLecture);
    }
}
