package com.swcampus.domain.lecture;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureService {

	private final LectureRepository lectureRepository;
	private final com.swcampus.domain.teacher.TeacherRepository teacherRepository;

	public Lecture registerLecture(Lecture lecture) {
		// 0. 강사 정보 처리
		// - ID가 있으면: 기존 강사 검색 (없으면 예외 처리 또는 무시)
		// - ID가 없으면: 신규 생성
		List<com.swcampus.domain.teacher.Teacher> managedTeachers = lecture.getTeachers().stream()
				.map(t -> {
					if (t.getTeacherId() != null) {
						return teacherRepository.findById(t.getTeacherId())
								.orElseThrow(() -> new ResourceNotFoundException(
										"Teacher not found with id: " + t.getTeacherId()));
					} else {
						return teacherRepository.save(t);
					}
				})
				.toList();

		// 1. 저장
		// 도메인 객체 다시 조립 (관리된 강사 리스트로 교체)
		Lecture newLecture = lecture.toBuilder()
				.teachers(managedTeachers)
				.build();

		return lectureRepository.save(newLecture);
	}

	public Lecture getLecture(Long lectureId) {
		return lectureRepository.findById(lectureId)
				.orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));
	}

}