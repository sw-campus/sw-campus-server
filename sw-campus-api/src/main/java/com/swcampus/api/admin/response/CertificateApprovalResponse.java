package com.swcampus.api.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수료증 승인 처리 응답")
public record CertificateApprovalResponse(
    @Schema(description = "수료증 ID", example = "1")
    Long certificateId,

    @Schema(description = "승인 상태 (APPROVED, REJECTED)", example = "APPROVED")
    String approvalStatus,

    @Schema(description = "처리 결과 메시지", example = "수료증이 승인되었습니다.")
    String message
) {}
