package com.swcampus.domain.auth;

import java.util.Optional;

public interface EmailVerificationRepository {
    EmailVerification save(EmailVerification emailVerification);
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findByToken(String token);
    Optional<EmailVerification> findByEmailAndVerified(String email, boolean verified);
    void deleteByEmail(String email);
}
