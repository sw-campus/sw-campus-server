package com.swcampus.domain.review;

public record ReviewEligibility(
    boolean hasNickname,
    boolean hasCertificate,
    boolean canWrite,
    boolean eligible
) {
    public static ReviewEligibility of(boolean hasNickname, boolean hasCertificate, boolean canWrite) {
        return new ReviewEligibility(
                hasNickname,
                hasCertificate,
                canWrite,
                hasNickname && hasCertificate && canWrite
        );
    }
}
