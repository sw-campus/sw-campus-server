package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.EmailVerificationExpiredException;
import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService 테스트")
class EmailServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Nested
    @DisplayName("인증 메일 발송")
    class SendVerificationEmail {

        @Test
        @DisplayName("인증 메일을 발송할 수 있다 - 일반 회원가입")
        void success_personal() throws Exception {
            // given
            String email = "user@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(false);
            when(emailVerificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            
            // frontendUrl 설정
            setField(emailService, "frontendUrl", "http://localhost:3000");

            // when
            emailService.sendVerificationEmail(email, "personal");

            // then
            ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
            verify(emailVerificationRepository).save(captor.capture());
            
            EmailVerification saved = captor.getValue();
            assertThat(saved.getEmail()).isEqualTo(email);
            assertThat(saved.getToken()).isNotNull();
            assertThat(saved.getToken()).hasSize(36); // UUID
            assertThat(saved.isVerified()).isFalse();
            
            // 인증 링크에 type=personal 포함 확인
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(mailSender).send(eq(email), anyString(), contentCaptor.capture());
            assertThat(contentCaptor.getValue()).contains("type=personal");
        }

        @Test
        @DisplayName("인증 메일을 발송할 수 있다 - 기관 회원가입")
        void success_organization() throws Exception {
            // given
            String email = "org@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(false);
            when(emailVerificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            
            // frontendUrl 설정
            setField(emailService, "frontendUrl", "http://localhost:3000");

            // when
            emailService.sendVerificationEmail(email, "organization");

            // then
            ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
            verify(emailVerificationRepository).save(captor.capture());
            
            EmailVerification saved = captor.getValue();
            assertThat(saved.getEmail()).isEqualTo(email);
            
            // 인증 링크에 type=organization 포함 확인
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(mailSender).send(eq(email), anyString(), contentCaptor.capture());
            assertThat(contentCaptor.getValue()).contains("type=organization");
        }

        @Test
        @DisplayName("이미 가입된 이메일은 인증 발송 실패")
        void fail_alreadyRegistered() {
            // given
            String email = "user@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> emailService.sendVerificationEmail(email, "personal"))
                    .isInstanceOf(DuplicateEmailException.class);
            
            verify(emailVerificationRepository, never()).save(any());
            verify(mailSender, never()).send(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("재발송 시 기존 인증 정보를 삭제한다")
        void deleteExistingVerification() throws Exception {
            // given
            String email = "user@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(false);
            when(emailVerificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            setField(emailService, "frontendUrl", "http://localhost:3000");

            // when
            emailService.sendVerificationEmail(email, "personal");

            // then
            verify(emailVerificationRepository).deleteByEmail(email);
            verify(emailVerificationRepository).save(any(EmailVerification.class));
        }
    }

    @Nested
    @DisplayName("이메일 인증 검증")
    class VerifyEmail {

        @Test
        @DisplayName("유효한 토큰으로 이메일 인증을 완료할 수 있다")
        void success() {
            // given
            String token = "valid-token-uuid";
            EmailVerification verification = EmailVerification.create("user@example.com");
            when(emailVerificationRepository.findByToken(token))
                    .thenReturn(Optional.of(verification));
            when(emailVerificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // when
            emailService.verifyEmail(token);

            // then
            assertThat(verification.isVerified()).isTrue();
            verify(emailVerificationRepository).save(verification);
        }

        @Test
        @DisplayName("존재하지 않는 토큰이면 실패")
        void fail_invalidToken() {
            // given
            String token = "invalid-token";
            when(emailVerificationRepository.findByToken(token))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> emailService.verifyEmail(token))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("만료된 토큰이면 실패")
        void fail_expired() {
            // given
            String token = "expired-token";
            // 만료된 인증 정보 생성
            EmailVerification verification = EmailVerification.of(
                    1L, "user@example.com", token, false,
                    LocalDateTime.now().minusHours(1), // 이미 만료됨
                    LocalDateTime.now().minusHours(25)
            );
            when(emailVerificationRepository.findByToken(token))
                    .thenReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> emailService.verifyEmail(token))
                    .isInstanceOf(EmailVerificationExpiredException.class);
        }
    }

    @Nested
    @DisplayName("인증 상태 확인")
    class CheckVerificationStatus {

        @Test
        @DisplayName("인증 완료된 이메일은 true 반환")
        void verified() {
            // given
            String email = "user@example.com";
            EmailVerification verification = EmailVerification.create(email);
            verification.verify();
            when(memberRepository.existsByEmail(email)).thenReturn(false);
            when(emailVerificationRepository.findByEmailAndVerified(email, true))
                    .thenReturn(Optional.of(verification));

            // when
            boolean isVerified = emailService.isEmailVerified(email);

            // then
            assertThat(isVerified).isTrue();
        }

        @Test
        @DisplayName("인증되지 않은 이메일은 false 반환")
        void notVerified() {
            // given
            String email = "user@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(false);
            when(emailVerificationRepository.findByEmailAndVerified(email, true))
                    .thenReturn(Optional.empty());

            // when
            boolean isVerified = emailService.isEmailVerified(email);

            // then
            assertThat(isVerified).isFalse();
        }

        @Test
        @DisplayName("인증 정보가 없으면 false 반환")
        void notFound() {
            // given
            String email = "user@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(false);
            when(emailVerificationRepository.findByEmailAndVerified(email, true))
                    .thenReturn(Optional.empty());

            // when
            boolean isVerified = emailService.isEmailVerified(email);

            // then
            assertThat(isVerified).isFalse();
        }

        @Test
        @DisplayName("이미 가입된 회원의 이메일은 false 반환 (재가입 방지)")
        void existingMember_returnsFalse() {
            // given
            String email = "existing@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(true);

            // when
            boolean isVerified = emailService.isEmailVerified(email);

            // then
            assertThat(isVerified).isFalse();
            // emailVerificationRepository는 호출되지 않아야 함
            verify(emailVerificationRepository, never()).findByEmailAndVerified(anyString(), eq(true));
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
