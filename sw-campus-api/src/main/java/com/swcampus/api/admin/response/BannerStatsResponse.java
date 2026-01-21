package com.swcampus.api.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 배너 상태별 통계 응답 DTO
 */
@Getter
@Builder
@Schema(description = "배너 상태별 통계 응답")
public class BannerStatsResponse {

    @Schema(description = "전체 배너 수", example = "20")
    private final long total;

    @Schema(description = "활성 배너 수", example = "15")
    private final long active;

    @Schema(description = "비활성 배너 수", example = "5")
    private final long inactive;

    @Schema(description = "예정 배너 수 (기간 기준)", example = "3")
    private final long scheduled;

    @Schema(description = "현재 진행 중 배너 수 (기간 기준)", example = "10")
    private final long currentlyActive;

    @Schema(description = "종료된 배너 수 (기간 기준)", example = "7")
    private final long ended;

    public static BannerStatsResponse of(long total, long active, long inactive, 
            long scheduled, long currentlyActive, long ended) {
        return BannerStatsResponse.builder()
                .total(total)
                .active(active)
                .inactive(inactive)
                .scheduled(scheduled)
                .currentlyActive(currentlyActive)
                .ended(ended)
                .build();
    }
}
