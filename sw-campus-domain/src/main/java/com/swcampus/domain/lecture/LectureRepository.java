package com.swcampus.domain.lecture;

import java.util.Optional;

public interface LectureRepository {
	Lecture save(Lecture lecture);

	Optional<Lecture> findById(Long id);
}