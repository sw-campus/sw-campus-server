package com.swcampus.api.certificate.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수료증 이미지 수정 응답")
public record CertificateImageUpdateResponse(
    @Schema(description = "수료증 ID", example = "1")
    Long certificateId,

    @Schema(description = "새 수료증 이미지 키", example = "certificates/2024/01/01/uuid.jpg")
    String imageKey,

    @Schema(description = "승인 상태 (PENDING으로 초기화됨)", example = "PENDING")
    String approvalStatus,

    @Schema(description = "안내 메시지", example = "수료증 이미지가 수정되었습니다. 관리자 재승인이 필요합니다.")
    String message
) {
    public static CertificateImageUpdateResponse of(Long certificateId, String imageKey, String approvalStatus) {
        return new CertificateImageUpdateResponse(
            certificateId,
            imageKey,
            approvalStatus,
            "수료증 이미지가 수정되었습니다. 관리자 재승인이 필요합니다."
        );
    }
}
