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

import com.swcampus.api.admin.response.ApprovalStatsResponse;
import com.swcampus.api.organization.response.AdminOrganizationApprovalResponse;
import com.swcampus.api.organization.response.AdminOrganizationDetailResponse;
import com.swcampus.api.organization.response.AdminOrganizationSummaryResponse;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.organization.AdminOrganizationService;
import com.swcampus.domain.storage.PresignedUrlService;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.organization.ApproveOrganizationResult;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.RejectOrganizationResult;

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
    private final EmailService emailService;
    private final PresignedUrlService presignedUrlService;

    @Operation(summary = "기관 상태별 통계 조회", description = "전체/대기/승인/반려 기관 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<ApprovalStatsResponse> getStats() {
        var stats = adminOrganizationService.getStats();
        return ResponseEntity.ok(ApprovalStatsResponse.of(stats.total(), stats.pending(), stats.approved(), stats.rejected()));
    }

    @Operation(summary = "기관 목록 조회/검색", description = "기관 목록을 조회하고 검색합니다. 상태와 기관명으로 필터링할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<AdminOrganizationSummaryResponse>> getOrganizations(
            @Parameter(description = "승인 상태 (PENDING, APPROVED, REJECTED), 미입력시 전체") @RequestParam(name = "status", required = false) ApprovalStatus status,
            @Parameter(description = "검색 키워드 (기관명), 미입력시 전체") @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        Page<Organization> organizations = adminOrganizationService.searchOrganizations(status, keyword, pageable);
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

        // Private bucket의 재직증명서 이미지에 접근하기 위한 presigned URL 생성
        String certificateUrl = null;
        if (organization.getCertificateKey() != null) {
            certificateUrl = presignedUrlService.getPresignedUrl(organization.getCertificateKey(), true).url();
        }

        return ResponseEntity.ok(AdminOrganizationDetailResponse.from(organization, certificateUrl));
    }

    @Operation(summary = "기관 승인", description = "기관 가입을 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<AdminOrganizationApprovalResponse> approveOrganization(
            @Parameter(description = "기관 ID") @PathVariable("id") Long id) {
        ApproveOrganizationResult result = adminOrganizationService.approveOrganization(id);

        // 승인된 사용자에게 이메일 발송
        emailService.sendApprovalEmail(
                result.getMemberEmail(),
                result.getOrganization().getName()
        );

        return ResponseEntity.ok(AdminOrganizationApprovalResponse.of(result.getOrganization(), "기관이 승인되었습니다."));
    }

    @Operation(summary = "기관 반려", description = "기관 가입을 반려합니다. 해당 기관에 연결된 회원을 삭제하고 관리자 연락처를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공",
                    content = @Content(schema = @Schema(implementation = AdminOrganizationApprovalResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"id\": 1, \"approvalStatus\": \"REJECTED\", \"message\": \"기관이 반려되었습니다. 관리자에게 문의해 주세요.\", \"adminEmail\": \"admin@example.com\", \"adminPhone\": \"010-1234-5678\"}"))),
            @ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<AdminOrganizationApprovalResponse> rejectOrganization(
            @Parameter(description = "기관 ID") @PathVariable("id") Long id) {
        RejectOrganizationResult result = adminOrganizationService.rejectOrganization(id);

        // 반려된 사용자에게 이메일 발송
        emailService.sendRejectionEmail(
                result.getMemberEmail(),
                result.getAdminEmail(),
                result.getAdminPhone()
        );

        return ResponseEntity.ok(AdminOrganizationApprovalResponse.ofReject(id, result, "기관이 반려되었습니다. 관리자에게 문의해 주세요."));
    }
}
