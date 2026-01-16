package com.swcampus.api.admin;

import com.swcampus.api.admin.response.TestDataCreateResponse;
import com.swcampus.api.admin.response.TestDataSummaryResponse;
import com.swcampus.domain.testdata.TestDataCreateResult;
import com.swcampus.domain.testdata.TestDataService;
import com.swcampus.domain.testdata.TestDataSummary;
import com.swcampus.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Test Data", description = "관리자 테스트 데이터 관리 API")
@RestController
@RequestMapping("/api/v1/admin/test-data")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class AdminTestDataController {

    private final TestDataService testDataService;

    @Operation(summary = "테스트 데이터 생성",
            description = "테스트용 기관, 강의, 회원, 수료증, 리뷰 데이터를 생성합니다. " +
                    "이미 테스트 데이터가 존재하면 먼저 삭제해야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "이미 테스트 데이터가 존재함",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 400, "message": "이미 테스트 데이터가 존재합니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
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
    @PostMapping
    public ResponseEntity<TestDataCreateResponse> createTestData() {
        TestDataCreateResult result = testDataService.createTestData();
        return ResponseEntity.ok(TestDataCreateResponse.from(result));
    }

    @Operation(summary = "테스트 데이터 삭제",
            description = "생성된 모든 테스트 데이터를 삭제합니다. " +
                    "실제 사용자 데이터는 삭제되지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "삭제할 테스트 데이터가 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"status": 400, "message": "삭제할 테스트 데이터가 없습니다", "timestamp": "2025-12-09T12:00:00"}
                                    """))),
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
    @DeleteMapping
    public ResponseEntity<Void> deleteTestData() {
        testDataService.deleteTestData();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "테스트 데이터 현황 조회",
            description = "현재 생성된 테스트 데이터의 현황을 조회합니다.")
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
    @GetMapping("/summary")
    public ResponseEntity<TestDataSummaryResponse> getSummary() {
        TestDataSummary summary = testDataService.getSummary();
        return ResponseEntity.ok(TestDataSummaryResponse.from(summary));
    }
}
