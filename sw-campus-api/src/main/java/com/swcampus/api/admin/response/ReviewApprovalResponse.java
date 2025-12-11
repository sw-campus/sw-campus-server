package com.swcampus.api.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "후기 승인 처리 응답")
public record ReviewApprovalResponse(
    @Schema(description = "후기 ID", example = "1")
    Long reviewId,

    @Schema(description = "승인 상태 (APPROVED, REJECTED)", example = "APPROVED")
    String approvalStatus,

    @Schema(description = "처리 결과 메시지", example = "후기가 승인되었습니다.")
    String message
) {}
