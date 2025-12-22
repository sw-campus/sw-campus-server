package com.swcampus.api.certificate.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수료증 인증 여부 응답")
public record CertificateCheckResponse(
    @Schema(description = "수료증 인증 여부", example = "true")
    boolean certified,

    @Schema(description = "수료증 ID", example = "1")
    Long certificateId,

    @Schema(description = "수료증 이미지 S3 Key", example = "certificates/2024/01/01/uuid.jpg")
    String imageKey,

    @Schema(description = "승인 상태 (PENDING, APPROVED, REJECTED)", example = "APPROVED")
    String approvalStatus,

    @Schema(description = "인증 일시", example = "2025-01-15T10:30:00")
    String certifiedAt
) {
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static CertificateCheckResponse notCertified() {
        return new CertificateCheckResponse(false, null, null, null, null);
    }

    public static CertificateCheckResponse certified(Long id, String imageKey,
                                                      String status, LocalDateTime certifiedAt) {
        return new CertificateCheckResponse(
            true,
            id,
            imageKey,
            status,
            certifiedAt != null ? certifiedAt.format(FORMATTER) : null
        );
    }
}
