package com.swcampus.api.certificate.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record CertificateCheckResponse(
    boolean certified,
    Long certificateId,
    String imageUrl,
    String approvalStatus,
    String certifiedAt
) {
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static CertificateCheckResponse notCertified() {
        return new CertificateCheckResponse(false, null, null, null, null);
    }

    public static CertificateCheckResponse certified(Long id, String imageUrl, 
                                                      String status, LocalDateTime certifiedAt) {
        return new CertificateCheckResponse(
            true, 
            id, 
            imageUrl, 
            status, 
            certifiedAt != null ? certifiedAt.format(FORMATTER) : null
        );
    }
}
