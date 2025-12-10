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

    Page<Lecture> searchLectures(LectureSearchCondition condition);

    List<Lecture> findAllExpiredAndRecruiting(LocalDateTime now);

    List<Lecture> findAllByOrgId(Long orgId);

    Map<Long, Long> countLecturesByStatusGroupByOrg(LectureStatus status);
}