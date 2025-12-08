package com.swcampus.api.lecture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.lecture.request.LectureCreateRequest;
import com.swcampus.api.lecture.response.LectureResponse;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lectures")
@RequiredArgsConstructor
public class LectureController {

	private final LectureService lectureService;

	@PostMapping
	public ResponseEntity<LectureResponse> createLecture(@RequestBody LectureCreateRequest request) {
		Lecture lectureDomain = request.toDomain();
		Lecture savedLecture = lectureService.registerLecture(lectureDomain);

		return ResponseEntity.ok(LectureResponse.from(savedLecture));
	}

	@GetMapping("/{lectureId}")
	public ResponseEntity<LectureResponse> getLecture(@PathVariable Long lectureId) {
		Lecture lecture = lectureService.getLecture(lectureId);
		return ResponseEntity.ok(LectureResponse.from(lecture));
	}
}