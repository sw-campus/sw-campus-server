package com.swcampus.domain.lecture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;

import java.util.List;

import com.swcampus.domain.lecture.dto.LectureSearchCondition;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureService {

	private final LectureRepository lectureRepository;


	public Lecture registerLecture(Lecture lecture) {
		Lecture newLecture = lecture.toBuilder()
			.lectureAuthStatus(LectureAuthStatus.PENDING)
			.build();
		return lectureRepository.save(newLecture);
	}

	public Lecture getLecture(Long lectureId) {
		return lectureRepository.findById(lectureId)
				.orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));
	}

	@Transactional(readOnly = true)
	public Page<Lecture> searchLectures(LectureSearchCondition condition) {
		return lectureRepository.searchLectures(condition);
	}

}