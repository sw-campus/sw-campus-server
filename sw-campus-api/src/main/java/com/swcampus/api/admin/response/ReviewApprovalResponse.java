package com.swcampus.api.admin.response;

public record ReviewApprovalResponse(
    Long reviewId,
    String approvalStatus,
    String message
) {}
