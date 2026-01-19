package com.swcampus.api.admin;

import com.swcampus.api.admin.request.*;
import com.swcampus.api.admin.response.*;
import com.swcampus.api.exception.ErrorResponse;
import com.swcampus.domain.survey.*;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Admin Question Set", description = "관리자 문항 세트 관리 API")
@RestController
@RequestMapping("/api/v1/admin/survey/question-sets")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionSetController {

    private final AdminSurveyQuestionService adminSurveyQuestionService;

    // ===== QuestionSet CRUD =====

    @Operation(summary = "문항 세트 목록 조회", description = "전체 문항 세트를 조회합니다. type 파라미터로 필터링 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<AdminQuestionSetResponse>> getQuestionSets(
            @Parameter(description = "세트 타입 (BASIC, APTITUDE)")
            @RequestParam(name = "type", required = false) QuestionSetType type
    ) {
        List<SurveyQuestionSet> questionSets;
        if (type != null) {
            questionSets = adminSurveyQuestionService.getQuestionSetsByType(type);
        } else {
            questionSets = adminSurveyQuestionService.getAllQuestionSets();
        }

        List<AdminQuestionSetResponse> response = questionSets.stream()
                .map(AdminQuestionSetResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "문항 세트 상세 조회", description = "문항 세트와 문항, 선택지를 포함한 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 404, "message": "문항 세트를 찾을 수 없습니다", "timestamp": "2025-12-09T12:00:00"}
                                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminQuestionSetDetailResponse> getQuestionSet(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("id") Long id
    ) {
        SurveyQuestionSet questionSet = adminSurveyQuestionService.getQuestionSetWithQuestions(id);
        return ResponseEntity.ok(AdminQuestionSetDetailResponse.from(questionSet));
    }

    @Operation(summary = "Part별 문항 수 조회", description = "문항 세트의 Part별 문항 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/question-counts")
    public ResponseEntity<Map<QuestionPart, Integer>> getQuestionCountsByPart(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("id") Long id
    ) {
        Map<QuestionPart, Integer> counts = adminSurveyQuestionService.getQuestionCountsByPart(id);
        return ResponseEntity.ok(counts);
    }

    @Operation(summary = "문항 세트 생성", description = "새 문항 세트를 DRAFT 상태로 생성합니다. (성향 테스트 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AdminQuestionSetResponse> createQuestionSet(
            @Valid @RequestBody CreateQuestionSetRequest request
    ) {
        SurveyQuestionSet questionSet = adminSurveyQuestionService.createQuestionSet(
                request.name(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminQuestionSetResponse.from(questionSet));
    }

    @Operation(summary = "문항 세트 수정", description = "문항 세트의 이름과 설명을 수정합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminQuestionSetResponse> updateQuestionSet(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody UpdateQuestionSetRequest request
    ) {
        SurveyQuestionSet questionSet = adminSurveyQuestionService.updateQuestionSet(
                id,
                request.name(),
                request.description()
        );
        return ResponseEntity.ok(AdminQuestionSetResponse.from(questionSet));
    }

    @Operation(summary = "문항 세트 삭제", description = "문항 세트를 삭제합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 삭제 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionSet(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("id") Long id
    ) {
        adminSurveyQuestionService.deleteQuestionSet(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "문항 세트 발행", description = "문항 세트를 발행합니다. 기존 PUBLISHED 세트는 ARCHIVED로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발행 성공"),
            @ApiResponse(responseCode = "400", description = "발행 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/publish")
    public ResponseEntity<AdminQuestionSetResponse> publishQuestionSet(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("id") Long id
    ) {
        SurveyQuestionSet questionSet = adminSurveyQuestionService.publishQuestionSet(id);
        return ResponseEntity.ok(AdminQuestionSetResponse.from(questionSet));
    }

    @Operation(summary = "문항 세트 재발행", description = "ARCHIVED 상태의 문항 세트를 다시 발행합니다. 기존 PUBLISHED 세트는 ARCHIVED로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발행 성공"),
            @ApiResponse(responseCode = "400", description = "ARCHIVED 상태가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/republish")
    public ResponseEntity<AdminQuestionSetResponse> republishQuestionSet(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("id") Long id
    ) {
        SurveyQuestionSet questionSet = adminSurveyQuestionService.republishQuestionSet(id);
        return ResponseEntity.ok(AdminQuestionSetResponse.from(questionSet));
    }

    @Operation(summary = "문항 세트 복제", description = "기존 문항 세트를 복제하여 새 버전을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "복제 성공"),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/clone")
    public ResponseEntity<AdminQuestionSetResponse> cloneQuestionSet(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("id") Long id
    ) {
        SurveyQuestionSet questionSet = adminSurveyQuestionService.cloneQuestionSet(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminQuestionSetResponse.from(questionSet));
    }

    // ===== Question CRUD =====

    @Operation(summary = "문항 추가", description = "문항 세트에 새 문항을 추가합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항 세트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{setId}/questions")
    public ResponseEntity<AdminQuestionResponse> addQuestion(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("setId") Long setId,
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        SurveyQuestion question = adminSurveyQuestionService.addQuestion(
                setId,
                request.questionText(),
                request.questionType(),
                request.isRequired(),
                request.part(),
                request.showCondition(),
                request.metadata()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminQuestionResponse.from(question));
    }

    @Operation(summary = "문항 수정", description = "문항을 수정합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{setId}/questions/{questionId}")
    public ResponseEntity<AdminQuestionResponse> updateQuestion(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("setId") Long setId,
            @Parameter(description = "문항 ID", required = true) @PathVariable("questionId") Long questionId,
            @Valid @RequestBody UpdateQuestionRequest request
    ) {
        SurveyQuestion question = adminSurveyQuestionService.updateQuestion(
                questionId,
                request.questionText(),
                request.questionType(),
                request.isRequired(),
                request.fieldKey(),
                request.part(),
                request.showCondition(),
                request.metadata()
        );
        return ResponseEntity.ok(AdminQuestionResponse.from(question));
    }

    @Operation(summary = "문항 삭제", description = "문항을 삭제합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{setId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("setId") Long setId,
            @Parameter(description = "문항 ID", required = true) @PathVariable("questionId") Long questionId
    ) {
        adminSurveyQuestionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "문항 순서 변경", description = "문항의 순서를 변경합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "순서 변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{setId}/questions/{questionId}/order")
    public ResponseEntity<Void> reorderQuestion(
            @Parameter(description = "문항 세트 ID", required = true) @PathVariable("setId") Long setId,
            @Parameter(description = "문항 ID", required = true) @PathVariable("questionId") Long questionId,
            @Valid @RequestBody ReorderRequest request
    ) {
        adminSurveyQuestionService.reorderQuestion(questionId, request.newOrder());
        return ResponseEntity.noContent().build();
    }

    // ===== Option CRUD =====

    @Operation(summary = "선택지 추가", description = "문항에 새 선택지를 추가합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문항을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/questions/{questionId}/options")
    public ResponseEntity<AdminOptionResponse> addOption(
            @Parameter(description = "문항 ID", required = true) @PathVariable("questionId") Long questionId,
            @Valid @RequestBody CreateOptionRequest request
    ) {
        SurveyOption option = adminSurveyQuestionService.addOption(
                questionId,
                request.optionText(),
                request.score(),
                request.jobType(),
                request.isCorrect()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminOptionResponse.from(option));
    }

    @Operation(summary = "선택지 수정", description = "선택지를 수정합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "선택지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/questions/{questionId}/options/{optionId}")
    public ResponseEntity<AdminOptionResponse> updateOption(
            @Parameter(description = "문항 ID", required = true) @PathVariable("questionId") Long questionId,
            @Parameter(description = "선택지 ID", required = true) @PathVariable("optionId") Long optionId,
            @Valid @RequestBody UpdateOptionRequest request
    ) {
        SurveyOption option = adminSurveyQuestionService.updateOption(
                optionId,
                request.optionText(),
                request.score(),
                request.jobType(),
                request.isCorrect()
        );
        return ResponseEntity.ok(AdminOptionResponse.from(option));
    }

    @Operation(summary = "선택지 삭제", description = "선택지를 삭제합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "선택지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/questions/{questionId}/options/{optionId}")
    public ResponseEntity<Void> deleteOption(
            @Parameter(description = "문항 ID", required = true) @PathVariable("questionId") Long questionId,
            @Parameter(description = "선택지 ID", required = true) @PathVariable("optionId") Long optionId
    ) {
        adminSurveyQuestionService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "선택지 순서 변경", description = "선택지의 순서를 변경합니다. DRAFT 상태에서만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "순서 변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "발행된 문항 세트는 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "선택지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/questions/{questionId}/options/{optionId}/order")
    public ResponseEntity<Void> reorderOption(
            @Parameter(description = "문항 ID", required = true) @PathVariable("questionId") Long questionId,
            @Parameter(description = "선택지 ID", required = true) @PathVariable("optionId") Long optionId,
            @Valid @RequestBody ReorderRequest request
    ) {
        adminSurveyQuestionService.reorderOption(optionId, request.newOrder());
        return ResponseEntity.noContent().build();
    }
}
