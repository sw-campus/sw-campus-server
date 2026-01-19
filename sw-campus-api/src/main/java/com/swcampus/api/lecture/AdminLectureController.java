package com.swcampus.api.lecture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.admin.response.ApprovalStatsResponse;
import com.swcampus.api.exception.ErrorResponse;
import com.swcampus.api.lecture.request.LectureUpdateRequest;
import com.swcampus.api.lecture.response.AdminLectureApprovalResponse;
import com.swcampus.api.lecture.response.AdminLectureSummaryResponse;
import com.swcampus.api.lecture.response.LectureResponse;
import com.swcampus.domain.lecture.AdminLectureService;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.AdminOrganizationService;
import com.swcampus.domain.organization.Organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/lectures")
@RequiredArgsConstructor
@Tag(name = "Admin Lecture", description = "관리자 강의 관리 API")
@SecurityRequirement(name = "cookieAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLectureController {

    private final AdminLectureService adminLectureService;
    private final AdminOrganizationService adminOrganizationService;
    private final LectureService lectureService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "강의 상태별 통계 조회", description = "전체/대기/승인/반려 강의 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 403, "message": "관리자 권한이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @GetMapping("/stats")
    public ResponseEntity<ApprovalStatsResponse> getStats() {
        var stats = adminLectureService.getStats();
        return ResponseEntity.ok(ApprovalStatsResponse.of(stats.total(), stats.pending(), stats.approved(), stats.rejected()));
    }

    @Operation(summary = "강의 목록 조회/검색", description = "강의 목록을 조회하고 검색합니다. 승인 상태와 강의명으로 필터링할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 403, "message": "관리자 권한이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @GetMapping
    public ResponseEntity<Page<AdminLectureSummaryResponse>> getLectures(
            @Parameter(description = "승인 상태 (PENDING, APPROVED, REJECTED), 미입력시 전체") @RequestParam(name = "status", required = false) LectureAuthStatus status,
            @Parameter(description = "검색 키워드 (강의명), 미입력시 전체") @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Lecture> lectures = adminLectureService.searchLectures(status, keyword, pageable);
        return ResponseEntity.ok(lectures.map(AdminLectureSummaryResponse::from));
    }

    @Operation(summary = "강의 상세 조회", description = "강의의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<LectureResponse> getLecture(
            @Parameter(description = "강의 ID") @PathVariable("id") Long id) {
        Lecture lecture = adminLectureService.getLectureDetail(id);
        Organization organization = adminOrganizationService.getOrganizationDetail(lecture.getOrgId());
        return ResponseEntity.ok(LectureResponse.from(lecture, organization));
    }

    @Operation(summary = "관리자 강의 수정", description = "관리자 권한으로 강의 정보를 수정합니다. 기관 변경도 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 403, "message": "관리자 권한이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
            @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 404, "message": "강의를 찾을 수 없습니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LectureResponse> updateLecture(
            @Parameter(description = "강의 ID") @PathVariable("id") Long id,
            @Parameter(description = "강의 정보 (JSON string)", schema = @Schema(implementation = LectureUpdateRequest.class)) @RequestPart("lecture") String lectureJson,
            @Parameter(description = "강의 대표 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image,
            @Parameter(description = "강사 이미지 파일 목록") @RequestPart(value = "teacherImages", required = false) List<MultipartFile> teacherImages)
            throws IOException {

        LectureUpdateRequest request = objectMapper.readValue(lectureJson, LectureUpdateRequest.class);

        Set<ConstraintViolation<LectureUpdateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Lecture lectureDomain = request.toDomain();

        byte[] imageContent = null;
        String imageName = null;
        String contentType = null;
        if (image != null && !image.isEmpty()) {
            imageContent = image.getBytes();
            imageName = image.getOriginalFilename();
            contentType = image.getContentType();
        }

        List<LectureService.ImageContent> teacherImageContents = processTeacherImages(teacherImages);

        // ADMIN 권한으로 수정 (memberId는 의미 없음, role=ADMIN으로 권한 체크 우회)
        Lecture updatedLecture = lectureService.modifyLecture(id, 0L, Role.ADMIN, lectureDomain,
                imageContent, imageName, contentType, teacherImageContents);

        return ResponseEntity.ok(LectureResponse.from(updatedLecture));
    }

    @Operation(summary = "강의 승인", description = "강의 등록을 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<AdminLectureApprovalResponse> approveLecture(
            @Parameter(description = "강의 ID") @PathVariable("id") Long id) {
        Lecture lecture = adminLectureService.approveLecture(id);
        return ResponseEntity.ok(AdminLectureApprovalResponse.of(lecture, "강의가 승인되었습니다."));
    }

    @Operation(summary = "강의 반려", description = "강의 등록을 반려합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공",
                    content = @Content(schema = @Schema(implementation = AdminLectureApprovalResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"lectureId\": 1, \"lectureAuthStatus\": \"REJECTED\", \"message\": \"강의가 반려되었습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<AdminLectureApprovalResponse> rejectLecture(
            @Parameter(description = "강의 ID") @PathVariable("id") Long id) {
        Lecture lecture = adminLectureService.rejectLecture(id);
        return ResponseEntity.ok(AdminLectureApprovalResponse.of(lecture, "강의가 반려되었습니다."));
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
                    teacherImageContents.add(new LectureService.ImageContent(null, null, null));
                }
            }
            if (allEmpty) {
                teacherImageContents.clear();
            }
        }
        return teacherImageContents;
    }
}
