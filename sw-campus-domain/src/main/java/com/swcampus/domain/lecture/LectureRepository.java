package com.swcampus.domain.lecture;

import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;

public interface LectureRepository {
	Lecture save(Lecture lecture);

    Optional<Lecture> findById(Long id);

    Page<Lecture> searchLectures(LectureSearchCondition condition);

    List<Lecture> findAllExpiredAndRecruiting(LocalDateTime now);
}