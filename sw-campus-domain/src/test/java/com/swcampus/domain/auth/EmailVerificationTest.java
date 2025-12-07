package com.swcampus.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTest {

    @Test
    @DisplayName("이메일 인증 생성")
    void create() {
        // given
        String email = "test@example.com";

        // when
        EmailVerification ev = EmailVerification.create(email);

        // then
        assertThat(ev.getEmail()).isEqualTo(email);
        assertThat(ev.getToken()).isNotNull();
        assertThat(ev.getToken()).hasSize(36); // UUID format
        assertThat(ev.isVerified()).isFalse();
        assertThat(ev.getExpiresAt()).isNotNull();
        assertThat(ev.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("생성 시마다 다른 토큰 발급")
    void create_uniqueToken() {
        // given
        String email = "test@example.com";

        // when
        EmailVerification ev1 = EmailVerification.create(email);
        EmailVerification ev2 = EmailVerification.create(email);

        // then
        assertThat(ev1.getToken()).isNotEqualTo(ev2.getToken());
    }

    @Test
    @DisplayName("인증 완료 처리")
    void verify() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com");

        // when
        ev.verify();

        // then
        assertThat(ev.isVerified()).isTrue();
    }

    @Test
    @DisplayName("만료되지 않은 인증은 isExpired가 false")
    void isExpired_notExpired() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com");

        // when & then
        assertThat(ev.isExpired()).isFalse();
    }
}
