package com.swcampus.infra.postgres.oauth;

import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.SocialAccount;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static SocialAccountEntity from(SocialAccount socialAccount) {
        SocialAccountEntity entity = new SocialAccountEntity();
        entity.id = socialAccount.getId();
        entity.memberId = socialAccount.getMemberId();
        entity.provider = socialAccount.getProvider();
        entity.providerId = socialAccount.getProviderId();
        entity.createdAt = socialAccount.getCreatedAt();
        return entity;
    }

    public SocialAccount toDomain() {
        return SocialAccount.of(
            this.id,
            this.memberId,
            this.provider,
            this.providerId,
            this.createdAt
        );
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
