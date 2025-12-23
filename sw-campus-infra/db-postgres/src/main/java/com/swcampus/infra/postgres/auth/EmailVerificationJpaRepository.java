package com.swcampus.infra.postgres.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationEntity, Long> {
    Optional<EmailVerificationEntity> findByEmail(String email);
    Optional<EmailVerificationEntity> findByToken(String token);
    Optional<EmailVerificationEntity> findByEmailAndVerified(String email, boolean verified);
    void deleteByEmail(String email);
}
