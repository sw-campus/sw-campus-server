package com.swcampus.domain.lecture;

import java.util.Optional;

public interface LectureRepository {
	Lecture save(Lecture lecture);
	Optional<Lecture> findById(Long id);
	// 필요한 경우 리스트 조회, 필터링 메서드 추가
}