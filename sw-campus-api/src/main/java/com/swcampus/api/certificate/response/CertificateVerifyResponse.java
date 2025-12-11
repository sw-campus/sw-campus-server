package com.swcampus.api.certificate.response;

public record CertificateVerifyResponse(
    Long certificateId,
    Long lectureId,
    String imageUrl,
    String approvalStatus,
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
