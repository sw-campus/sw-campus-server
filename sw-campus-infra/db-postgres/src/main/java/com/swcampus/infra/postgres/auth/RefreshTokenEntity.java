package com.swcampus.infra.postgres.auth;

import com.swcampus.domain.auth.RefreshToken;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static RefreshTokenEntity from(RefreshToken rt) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.id = rt.getId();
        entity.memberId = rt.getMemberId();
        entity.token = rt.getToken();
        entity.expiresAt = rt.getExpiresAt();
        entity.createdAt = rt.getCreatedAt();
        return entity;
    }

    public RefreshToken toDomain() {
        return RefreshToken.of(
                id,
                memberId,
                token,
                expiresAt,
                createdAt
        );
    }
}
