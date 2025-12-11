package com.swcampus.api.admin.response;

public record CertificateApprovalResponse(
    Long certificateId,
    String approvalStatus,
    String message
) {}
