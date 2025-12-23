package com.swcampus.domain.certificate;

import com.swcampus.domain.common.ApprovalStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate {
    private Long id;
    private Long memberId;
    private Long lectureId;
    private String imageKey;
    private String status;
    private ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;

    public static Certificate create(Long memberId, Long lectureId, String imageKey, String status) {
        Certificate certificate = new Certificate();
        certificate.memberId = memberId;
        certificate.lectureId = lectureId;
        certificate.imageKey = imageKey;
        certificate.status = status;
        certificate.approvalStatus = ApprovalStatus.PENDING;
        certificate.createdAt = LocalDateTime.now();
        return certificate;
    }

    public static Certificate of(Long id, Long memberId, Long lectureId,
                                  String imageKey, String status,
                                  ApprovalStatus approvalStatus, LocalDateTime createdAt) {
        Certificate certificate = new Certificate();
        certificate.id = id;
        certificate.memberId = memberId;
        certificate.lectureId = lectureId;
        certificate.imageKey = imageKey;
        certificate.status = status;
        certificate.approvalStatus = approvalStatus;
        certificate.createdAt = createdAt;
        return certificate;
    }

    public void approve() {
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void reject() {
        this.approvalStatus = ApprovalStatus.REJECTED;
    }

    public boolean isPending() {
        return this.approvalStatus == ApprovalStatus.PENDING;
    }

    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }
}
