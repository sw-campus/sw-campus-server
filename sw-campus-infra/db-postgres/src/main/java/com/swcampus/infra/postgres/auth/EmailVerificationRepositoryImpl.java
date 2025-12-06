package com.swcampus.infra.postgres.auth;

import com.swcampus.domain.auth.EmailVerification;
import com.swcampus.domain.auth.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        EmailVerificationEntity entity = EmailVerificationEntity.from(emailVerification);
        EmailVerificationEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<EmailVerification> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(EmailVerificationEntity::toDomain);
    }

    @Override
    public void deleteByEmail(String email) {
        jpaRepository.deleteByEmail(email);
    }
}
