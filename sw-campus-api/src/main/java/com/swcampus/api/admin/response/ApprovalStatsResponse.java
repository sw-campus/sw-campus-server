package com.swcampus.api.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 승인 관리 페이지 통계 응답 DTO
 * 전체/대기/승인/반려 수를 반환
 */
@Getter
@Builder
@Schema(description = "승인 관리 상태별 통계 응답")
public class ApprovalStatsResponse {

    @Schema(description = "전체 수", example = "100")
    private final long total;

    @Schema(description = "승인 대기 수", example = "10")
    private final long pending;

    @Schema(description = "승인 완료 수", example = "85")
    private final long approved;

    @Schema(description = "반려 수", example = "5")
    private final long rejected;

    public static ApprovalStatsResponse of(long total, long pending, long approved, long rejected) {
        return ApprovalStatsResponse.builder()
                .total(total)
                .pending(pending)
                .approved(approved)
                .rejected(rejected)
                .build();
    }
}
