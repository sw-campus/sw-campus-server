package com.swcampus.domain.auth;

import java.util.Optional;

public interface EmailVerificationRepository {
    EmailVerification save(EmailVerification emailVerification);
    Optional<EmailVerification> findByEmail(String email);
    void deleteByEmail(String email);
}
