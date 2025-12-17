package com.swcampus.api.lecture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.lecture.request.LectureCreateRequest;
import com.swcampus.api.lecture.request.LectureSearchRequest;
import com.swcampus.api.lecture.request.LectureUpdateRequest;
import com.swcampus.api.lecture.response.LectureResponse;
import com.swcampus.api.lecture.response.LectureSummaryResponse;
import com.swcampus.api.review.response.ReviewResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.review.ReviewService;
import com.swcampus.domain.review.ReviewWithNickname;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Validator;

@RestController
@RequestMapping("/api/v1/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture", description = "강의 관리 API")
public class LectureController {

	private static final int TOP_RATED_LECTURE_COUNT = 4;

	private final LectureService lectureService;
	private final OrganizationService organizationService;
	private final ReviewService reviewService;
	private final ObjectMapper objectMapper;
	private final Validator validator;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "강의 등록", description = "새로운 강의를 등록합니다. 기관 회원만 가능하며, 관리자 승인 후 노출됩니다.")
	@SecurityRequirement(name = "cookieAuth")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "등록 성공 (승인 대기)"),
			@ApiResponse(responseCode = "401", description = "인증 필요"),
			@ApiResponse(responseCode = "403", description = "기관 회원만 가능")
	})
	public ResponseEntity<LectureResponse> createLecture(
			@CurrentMember MemberPrincipal member,
			@Parameter(description = "강의 정보 (JSON string)", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LectureCreateRequest.class)) @RequestPart("lecture") String lectureJson,
			@Parameter(description = "강의 대표 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image,
			@Parameter(description = "강사 이미지 파일 목록 (신규 강사의 수와 일치해야 함)") @RequestPart(value = "teacherImages", required = false) java.util.List<MultipartFile> teacherImages)
			throws IOException {

		LectureCreateRequest request = objectMapper.readValue(lectureJson, LectureCreateRequest.class);

		Set<ConstraintViolation<LectureCreateRequest>> violations = validator
				.validate(request);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		// 현재 로그인한 사용자 ID 가져오기
		Long currentUserId = member.memberId();

		Organization organization = organizationService.getOrganizationByUserId(currentUserId);

		Lecture lectureDomain = request.toDomain().toBuilder()
				.orgId(organization.getId())
				.build();

		byte[] imageContent = null;
		String imageName = null;
		String contentType = null;
		if (image != null && !image.isEmpty()) {
			imageContent = image.getBytes();
			imageName = image.getOriginalFilename();
			contentType = image.getContentType();
		}

		List<LectureService.ImageContent> teacherImageContents = processTeacherImages(teacherImages);

		Lecture savedLecture = lectureService.registerLecture(lectureDomain, imageContent, imageName, contentType,
				teacherImageContents);

		return ResponseEntity.status(HttpStatus.CREATED).body(LectureResponse.from(savedLecture));
	}

	@PutMapping(value = "/{lectureId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "강의 수정", description = "강의 정보를 수정합니다. 작성자(기관)만 수정 가능합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "403", description = "권한 없음"),
			@ApiResponse(responseCode = "404", description = "강의 없음")
	})
	public ResponseEntity<LectureResponse> updateLecture(
			@CurrentMember MemberPrincipal member,
			@Parameter(description = "강의 ID", example = "1", required = true) @PathVariable Long lectureId,
			@Parameter(description = "강의 정보 (JSON string)", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LectureUpdateRequest.class)) @RequestPart("lecture") String lectureJson,
			@Parameter(description = "강의 대표 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image,
			@Parameter(description = "강사 이미지 파일 목록 (신규 강사의 수와 일치해야 함)") @RequestPart(value = "teacherImages", required = false) java.util.List<MultipartFile> teacherImages)
			throws IOException {

		LectureUpdateRequest request = objectMapper.readValue(lectureJson, LectureUpdateRequest.class);

		Set<ConstraintViolation<LectureUpdateRequest>> violations = validator
				.validate(request);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		// 현재 로그인한 사용자 ID 가져오기
		Long currentUserId = member.memberId();

		Organization organization = organizationService.getOrganizationByUserId(currentUserId);

		// 권한 확인 및 로직 수행은 Service로 위임
		Lecture lectureDomain = request.toDomain().toBuilder()
				.orgId(organization.getId())
				.build();

		byte[] imageContent = null;
		String imageName = null;
		String contentType = null;
		if (image != null && !image.isEmpty()) {
			imageContent = image.getBytes();
			imageName = image.getOriginalFilename();
			contentType = image.getContentType();
		}

		List<LectureService.ImageContent> teacherImageContents = processTeacherImages(teacherImages);

		Lecture updatedLecture = lectureService.modifyLecture(lectureId, organization.getId(), lectureDomain,
				imageContent,
				imageName,
				contentType,
				teacherImageContents);

		return ResponseEntity.ok(LectureResponse.from(updatedLecture));
	}

	@GetMapping("/{lectureId}")
	@Operation(summary = "강의 상세 조회", description = "강의의 상세 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "404", description = "강의 없음")
	})
	public ResponseEntity<LectureResponse> getLecture(
			@Parameter(description = "강의 ID", example = "1", required = true) @PathVariable Long lectureId) {
		var lectureSummary = lectureService.getLectureWithStats(lectureId);
		Organization organization = null;
		if (lectureSummary.lecture().getOrgId() != null) {
			organization = organizationService.getOrganization(lectureSummary.lecture().getOrgId());
		}
		return ResponseEntity.ok(LectureResponse.from(
				lectureSummary.lecture(),
				organization,
				lectureSummary.averageScore(),
				lectureSummary.reviewCount()));
	}

	@GetMapping("/search")
	@Operation(summary = "강의 검색", description = "다양한 조건으로 강의를 검색합니다. 키워드, 지역, 카테고리, 비용, 선발절차 등으로 필터링할 수 있습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검색 성공")
	})
	public ResponseEntity<Page<LectureSummaryResponse>> searchLectures(
			@Valid @ModelAttribute LectureSearchRequest request) {
		var lectures = lectureService.searchLecturesWithStats(request.toCondition());
		Page<LectureSummaryResponse> response = lectures
				.map(dto -> LectureSummaryResponse.from(dto.lecture(), dto.averageScore(), dto.reviewCount()));
		return ResponseEntity.ok(response);
	}

	@GetMapping("/category/{categoryId}/top-rated")
	@Operation(summary = "카테고리별 평점 높은 강의 조회", description = "특정 카테고리의 강의를 평점 높은 순으로 4개 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	public ResponseEntity<List<LectureSummaryResponse>> getTopRatedLecturesByCategory(
			@Parameter(description = "카테고리 ID", example = "1", required = true) @PathVariable Long categoryId) {
		var lectures = lectureService.getTopRatedLecturesByCategoryWithStats(categoryId, TOP_RATED_LECTURE_COUNT);
		List<LectureSummaryResponse> response = lectures.stream()
				.map(dto -> LectureSummaryResponse.from(dto.lecture(), dto.averageScore(), dto.reviewCount()))
				.toList();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{lectureId}/reviews")
	@Operation(summary = "강의별 승인된 후기 조회", description = "강의 ID로 승인된 후기 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	public ResponseEntity<List<ReviewResponse>> getApprovedReviewsByLecture(
			@Parameter(description = "강의 ID", example = "1", required = true) @PathVariable Long lectureId) {
		List<ReviewWithNickname> reviewsWithNicknames = reviewService.getApprovedReviewsWithNicknameByLecture(lectureId);
		List<ReviewResponse> responses = reviewsWithNicknames.stream()
				.map(rwn -> ReviewResponse.from(rwn.review(), rwn.nickname()))
				.toList();
		return ResponseEntity.ok(responses);
	}

	private List<LectureService.ImageContent> processTeacherImages(List<MultipartFile> teacherImages)
			throws IOException {
		List<LectureService.ImageContent> teacherImageContents = new ArrayList<>();
		if (teacherImages != null) {
			boolean allEmpty = true;
			for (MultipartFile file : teacherImages) {
				if (file != null && !file.isEmpty()) {
					allEmpty = false;
					teacherImageContents.add(new LectureService.ImageContent(file.getBytes(),
							file.getOriginalFilename(), file.getContentType()));
				} else {
					// 파일이 null이거나 비어있으면 빈 객체 추가 (인덱스 유지)
					teacherImageContents.add(new LectureService.ImageContent(null, null, null));
				}
			}
			// 만약 전송된 파일이 모두 빈 파일이라면 (예: 클라이언트가 빈 값을 전송함) 리스트를 비워서
			// Service에서 "이미지 없음(0개)"으로 처리하도록 함 -> 검증 통과
			if (allEmpty) {
				teacherImageContents.clear();
			}
		}
		return teacherImageContents;
	}
}