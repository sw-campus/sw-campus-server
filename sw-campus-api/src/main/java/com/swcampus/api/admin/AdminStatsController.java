package com.swcampus.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.admin.response.AdminStatsResponse;
import com.swcampus.api.exception.ErrorResponse;
import com.swcampus.domain.admin.AdminStats;
import com.swcampus.domain.admin.AdminStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 통계 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Stats", description = "관리자 대시보드 통계 API")
@SecurityRequirement(name = "cookieAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(summary = "대시보드 통계 조회", 
               description = "관리자 대시보드에 표시할 통계 데이터를 조회합니다. " +
                            "전체/대기중 회원, 기관, 강의, 수료증, 리뷰 수와 " +
                            "회원 역할별 분포를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AdminStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStats stats = adminStatsService.getStats();
        return ResponseEntity.ok(AdminStatsResponse.from(stats));
    }
}
