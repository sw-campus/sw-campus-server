package com.swcampus.api.admin.response;

public record AdminCertificateResponse(
    Long certificateId,
    Long lectureId,
    String lectureName,
    String imageUrl,
    String approvalStatus,
    String certifiedAt
) {}
