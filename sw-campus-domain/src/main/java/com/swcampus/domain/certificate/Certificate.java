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
    private ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;

    public static Certificate create(Long memberId, Long lectureId, String imageKey) {
        Certificate certificate = new Certificate();
        certificate.memberId = memberId;
        certificate.lectureId = lectureId;
        certificate.imageKey = imageKey;
        certificate.approvalStatus = ApprovalStatus.PENDING;
        certificate.createdAt = LocalDateTime.now();
        return certificate;
    }

    public static Certificate of(Long id, Long memberId, Long lectureId,
                                  String imageKey,
                                  ApprovalStatus approvalStatus, LocalDateTime createdAt) {
        Certificate certificate = new Certificate();
        certificate.id = id;
        certificate.memberId = memberId;
        certificate.lectureId = lectureId;
        certificate.imageKey = imageKey;
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

    /**
     * 수료증 이미지 수정 가능 여부 확인
     * APPROVED 상태가 아닌 경우에만 수정 가능
     */
    public boolean canEdit() {
        return this.approvalStatus != ApprovalStatus.APPROVED;
    }

    /**
     * 수료증 이미지 키 업데이트
     * 이미지 수정 시 상태를 PENDING으로 초기화하여 재검증 필요
     */
    public void updateImageKey(String newImageKey) {
        this.imageKey = newImageKey;
        this.approvalStatus = ApprovalStatus.PENDING;
    }
}
