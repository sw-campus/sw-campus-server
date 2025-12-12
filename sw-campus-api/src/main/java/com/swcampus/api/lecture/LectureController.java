package com.swcampus.api.lecture;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.lecture.request.LectureCreateRequest;
import com.swcampus.api.lecture.request.LectureSearchRequest;
import com.swcampus.api.lecture.response.LectureResponse;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture", description = "강의 관리 API")
public class LectureController {

	private final LectureService lectureService;
	private final OrganizationService organizationService;

	@PostMapping
	@Operation(summary = "강의 등록", description = "새로운 강의를 등록합니다. 기관 회원만 가능하며, 관리자 승인 후 노출됩니다.")
	@SecurityRequirement(name = "cookieAuth")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "등록 성공 (승인 대기)"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "403", description = "기관 회원만 가능")
	})
	public ResponseEntity<LectureResponse> createLecture(
			@Valid @RequestBody LectureCreateRequest request) {
		// 현재 로그인한 사용자 ID 가져오기
		Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();

		Organization organization = organizationService.getOrganizationByUserId(currentUserId);

		Lecture lectureDomain = request.toDomain().toBuilder()
				.orgId(organization.getId())
				.build();

		Lecture savedLecture = lectureService.registerLecture(lectureDomain);

		return ResponseEntity.status(HttpStatus.CREATED).body(LectureResponse.from(savedLecture));
	}

	@GetMapping("/{lectureId}")
	@Operation(summary = "강의 상세 조회", description = "강의의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "강의 없음")
	})
	public ResponseEntity<LectureResponse> getLecture(
			@Parameter(description = "강의 ID", example = "1", required = true)
			@PathVariable Long lectureId) {
		Lecture lecture = lectureService.getLecture(lectureId);
		return ResponseEntity.ok(LectureResponse.from(lecture));
	}

	@GetMapping("/search")
	@Operation(summary = "강의 검색", description = "다양한 조건으로 강의를 검색합니다. 키워드, 지역, 카테고리, 비용, 선발절차 등으로 필터링할 수 있습니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "검색 성공")
	})
	public ResponseEntity<Page<LectureResponse>> searchLectures(
			@Valid @ModelAttribute LectureSearchRequest request) {
		Page<Lecture> lectures = lectureService.searchLectures(request.toCondition());
		Page<LectureResponse> response = lectures.map(LectureResponse::from);
		return ResponseEntity.ok(response);
	}
}