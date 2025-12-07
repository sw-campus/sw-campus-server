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
        String code = "123456";

        // when
        EmailVerification ev = EmailVerification.create(email, code);

        // then
        assertThat(ev.getEmail()).isEqualTo(email);
        assertThat(ev.getCode()).isEqualTo(code);
        assertThat(ev.isVerified()).isFalse();
        assertThat(ev.getExpiresAt()).isNotNull();
        assertThat(ev.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("인증 코드 일치 확인")
    void matchCode() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com", "123456");

        // when & then
        assertThat(ev.matchCode("123456")).isTrue();
        assertThat(ev.matchCode("654321")).isFalse();
    }

    @Test
    @DisplayName("인증 완료 처리")
    void verify() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com", "123456");

        // when
        ev.verify();

        // then
        assertThat(ev.isVerified()).isTrue();
    }

    @Test
    @DisplayName("만료되지 않은 인증은 isExpired가 false")
    void isExpired_notExpired() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com", "123456");

        // when & then
        assertThat(ev.isExpired()).isFalse();
    }
}
