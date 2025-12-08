package com.swcampus.infra.postgres.auth;

import com.swcampus.domain.auth.EmailVerification;
import com.swcampus.domain.auth.EmailVerificationRepository;
import com.swcampus.infra.postgres.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestApplication.class)
@Import(EmailVerificationEntityRepository.class)
@ActiveProfiles("test")
class EmailVerificationRepositoryTest {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Test
    @DisplayName("이메일 인증 저장 및 조회")
    void saveAndFindByEmail() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com");

        // when
        EmailVerification saved = emailVerificationRepository.save(ev);
        Optional<EmailVerification> found = emailVerificationRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getToken()).isNotNull();
        assertThat(found.get().getToken()).hasSize(36); // UUID
        assertThat(found.get().isVerified()).isFalse();
    }

    @Test
    @DisplayName("토큰으로 조회")
    void findByToken() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com");
        EmailVerification saved = emailVerificationRepository.save(ev);

        // when
        Optional<EmailVerification> found = emailVerificationRepository.findByToken(saved.getToken());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("인증 완료된 이메일 조회")
    void findByEmailAndVerified() {
        // given
        EmailVerification ev = EmailVerification.create("test@example.com");
        ev.verify();
        emailVerificationRepository.save(ev);

        // when
        Optional<EmailVerification> found = emailVerificationRepository.findByEmailAndVerified("test@example.com", true);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().isVerified()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일 조회")
    void findByEmail_notFound() {
        // when
        Optional<EmailVerification> found = emailVerificationRepository.findByEmail("notfound@example.com");

        // then
        assertThat(found).isEmpty();
    }
}
