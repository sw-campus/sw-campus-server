package com.swcampus.api.lecture;

import com.swcampus.api.lecture.request.LectureCreateRequest;
import com.swcampus.api.lecture.response.LectureResponse;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService; // Domain Service (Interface or Class)
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lectures")
@RequiredArgsConstructor
public class LectureController {

	private final LectureService lectureService;

	@PostMapping
	public ResponseEntity<LectureResponse> createLecture(@RequestBody LectureCreateRequest request) {
		// 1. DTO -> Domain 변환
		Lecture lectureDomain = request.toDomain();

		// 2. 비즈니스 로직 실행 (저장)
		Lecture savedLecture = lectureService.registerLecture(lectureDomain);

		// 3. Domain -> DTO 변환 및 응답
		return ResponseEntity.ok(LectureResponse.from(savedLecture));
	}

	@GetMapping("/{lectureId}")
	public ResponseEntity<LectureResponse> getLecture(@PathVariable Long lectureId) {
		Lecture lecture = lectureService.getLecture(lectureId);
		return ResponseEntity.ok(LectureResponse.from(lecture));
	}
}