package com.swcampus.infra.postgres.auth;

import com.swcampus.domain.auth.EmailVerification;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static EmailVerificationEntity from(EmailVerification ev) {
        EmailVerificationEntity entity = new EmailVerificationEntity();
        entity.id = ev.getId();
        entity.email = ev.getEmail();
        entity.token = ev.getToken();
        entity.verified = ev.isVerified();
        entity.expiresAt = ev.getExpiresAt();
        entity.createdAt = ev.getCreatedAt();
        return entity;
    }

    public EmailVerification toDomain() {
        return EmailVerification.of(
                id,
                email,
                token,
                verified,
                expiresAt,
                createdAt
        );
    }
}
