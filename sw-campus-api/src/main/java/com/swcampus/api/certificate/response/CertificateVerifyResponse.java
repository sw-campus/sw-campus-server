package com.swcampus.api.certificate.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수료증 인증 결과 응답")
public record CertificateVerifyResponse(
    @Schema(description = "수료증 ID", example = "1")
    Long certificateId,

    @Schema(description = "강의 ID", example = "10")
    Long lectureId,

    @Schema(description = "수료증 이미지 URL", example = "https://example.com/cert.jpg")
    String imageUrl,

    @Schema(description = "승인 상태 (PENDING)", example = "PENDING")
    String approvalStatus,

    @Schema(description = "안내 메시지", example = "수료증 인증이 완료되었습니다. 관리자 승인 후 후기 작성이 가능합니다.")
    String message
) {
    public static CertificateVerifyResponse of(Long certificateId, Long lectureId, 
                                                String imageUrl, String approvalStatus) {
        return new CertificateVerifyResponse(
            certificateId,
            lectureId,
            imageUrl,
            approvalStatus,
            "수료증 인증이 완료되었습니다. 관리자 승인 후 후기 작성이 가능합니다."
        );
    }
}
