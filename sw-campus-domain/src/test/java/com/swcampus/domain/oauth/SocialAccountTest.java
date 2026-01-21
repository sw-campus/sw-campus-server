package com.swcampus.domain.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SocialAccount 도메인 테스트")
class SocialAccountTest {

    @Test
    @DisplayName("소셜 계정을 생성할 수 있다")
    void create() {
        // given
        Long memberId = 1L;
        OAuthProvider provider = OAuthProvider.GOOGLE;
        String providerId = "google-user-id-123";

        // when
        SocialAccount account = SocialAccount.create(memberId, provider, providerId);

        // then
        assertThat(account.getMemberId()).isEqualTo(memberId);
        assertThat(account.getProvider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(account.getProviderId()).isEqualTo(providerId);
        assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("of 메서드로 소셜 계정을 복원할 수 있다")
    void of() {
        // given
        Long id = 1L;
        Long memberId = 100L;
        OAuthProvider provider = OAuthProvider.GITHUB;
        String providerId = "github-456";

        // when
        SocialAccount account = SocialAccount.of(id, memberId, provider, providerId, null);

        // then
        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getMemberId()).isEqualTo(memberId);
        assertThat(account.getProvider()).isEqualTo(OAuthProvider.GITHUB);
        assertThat(account.getProviderId()).isEqualTo(providerId);
    }
}
