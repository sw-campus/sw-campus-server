package com.swcampus.domain.lecture;

import org.springframework.data.domain.Page;
import java.util.Optional;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;

public interface LectureRepository {
	Lecture save(Lecture lecture);

    Optional<Lecture> findById(Long id);

    Page<Lecture> searchLectures(LectureSearchCondition condition);
}