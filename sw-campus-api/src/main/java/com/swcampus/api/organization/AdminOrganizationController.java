package com.swcampus.api.organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.organization.response.AdminOrganizationApprovalResponse;
import com.swcampus.api.organization.response.AdminOrganizationDetailResponse;
import com.swcampus.api.organization.response.AdminOrganizationSummaryResponse;
import com.swcampus.domain.organization.AdminOrganizationService;
import com.swcampus.domain.organization.ApprovalStatus;
import com.swcampus.domain.organization.Organization;

import com.swcampus.api.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/organizations")
@RequiredArgsConstructor
@Tag(name = "Admin Organization", description = "관리자 기관 관리 API")
@SecurityRequirement(name = "cookieAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrganizationController {

    private final AdminOrganizationService adminOrganizationService;

    @Operation(summary = "승인 대기 기관 목록 조회", description = "상태별로 기관 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<AdminOrganizationSummaryResponse>> getOrganizationList(
            @Parameter(description = "승인 상태 (PENDING, APPROVED, REJECTED)") @RequestParam(defaultValue = "PENDING") ApprovalStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Organization> organizations = adminOrganizationService.getOrganizationsByStatus(status, pageable);
        return ResponseEntity.ok(organizations.map(AdminOrganizationSummaryResponse::from));
    }

    @Operation(summary = "기관 이름 검색", description = "기관명으로 기관을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<AdminOrganizationSummaryResponse>> searchOrganizations(
            @Parameter(description = "검색 키워드 (기관명)") @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Organization> organizations = adminOrganizationService.searchOrganizationsByName(keyword, pageable);
        return ResponseEntity.ok(organizations.map(AdminOrganizationSummaryResponse::from));
    }

    @Operation(summary = "기관 상세 조회", description = "기관의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminOrganizationDetailResponse> getOrganization(
            @Parameter(description = "기관 ID") @PathVariable("id") Long id) {
        Organization organization = adminOrganizationService.getOrganizationDetail(id);
        return ResponseEntity.ok(AdminOrganizationDetailResponse.from(organization));
    }

    @Operation(summary = "기관 승인", description = "기관 가입을 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<AdminOrganizationApprovalResponse> approveOrganization(
            @Parameter(description = "기관 ID") @PathVariable("id") Long id) {
        Organization organization = adminOrganizationService.approveOrganization(id);
        return ResponseEntity.ok(AdminOrganizationApprovalResponse.of(organization, "기관이 승인되었습니다."));
    }

    @Operation(summary = "기관 반려", description = "기관 가입을 반려합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공",
                    content = @Content(schema = @Schema(implementation = AdminOrganizationApprovalResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"id\": 1, \"approvalStatus\": \"REJECTED\", \"message\": \"기관이 반려되었습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<AdminOrganizationApprovalResponse> rejectOrganization(
            @Parameter(description = "기관 ID") @PathVariable("id") Long id) {
        Organization organization = adminOrganizationService.rejectOrganization(id);
        return ResponseEntity.ok(AdminOrganizationApprovalResponse.of(organization, "기관이 반려되었습니다."));
    }
}
