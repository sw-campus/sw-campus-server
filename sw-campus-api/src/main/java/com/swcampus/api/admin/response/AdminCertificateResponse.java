package com.swcampus.api.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자용 수료증 응답")
public record AdminCertificateResponse(
    @Schema(description = "수료증 ID", example = "1")
    Long certificateId,

    @Schema(description = "강의 ID", example = "10")
    Long lectureId,

    @Schema(description = "강의명", example = "웹 개발 부트캠프")
    String lectureName,

    @Schema(description = "수료증 이미지 S3 Key", example = "certificates/2024/01/01/uuid.jpg")
    String imageKey,

    @Schema(description = "승인 상태 (PENDING, APPROVED, REJECTED)", example = "PENDING")
    String approvalStatus,

    @Schema(description = "인증 일시", example = "2025-01-15T10:30:00")
    String certifiedAt
) {}
