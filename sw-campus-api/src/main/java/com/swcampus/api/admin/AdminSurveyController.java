package com.swcampus.api.admin;

import com.swcampus.api.survey.response.SurveyResponse;
import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyService;
import com.swcampus.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Survey", description = "관리자 설문조사 관리 API")
@RestController
@RequestMapping("/api/v1/admin/surveys")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSurveyController {

    private final MemberSurveyService surveyService;

    @Operation(summary = "전체 설문조사 목록 조회", description = "모든 회원의 설문조사를 페이징 조회합니다.")
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
    public ResponseEntity<Page<SurveyResponse>> getSurveys(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MemberSurvey> surveys = surveyService.getAllSurveys(pageable);
        Page<SurveyResponse> response = surveys.map(SurveyResponse::from);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 회원 설문조사 조회", description = "특정 회원의 설문조사를 조회합니다.")
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
                                    """))),
            @ApiResponse(responseCode = "404", description = "설문조사 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 404, "message": "설문조사를 찾을 수 없습니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @GetMapping("/members/{memberId}")
    public ResponseEntity<SurveyResponse> getSurvey(
            @Parameter(description = "회원 ID", example = "1")
            @PathVariable("memberId") Long memberId
    ) {
        MemberSurvey survey = surveyService.getSurveyByMemberId(memberId);
        return ResponseEntity.ok(SurveyResponse.from(survey));
    }
}
