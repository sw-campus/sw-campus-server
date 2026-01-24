package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.EmailNotVerifiedException;
import com.swcampus.domain.auth.exception.InvalidPasswordException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - 회원가입 테스트")
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Spy
    private PasswordValidator passwordValidator = new PasswordValidator();

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("일반 회원가입에 성공하고 isFirstLogin이 true이다")
        void success() {
            // given
            SignupCommand command = SignupCommand.builder()
                    .email("user@example.com")
                    .password("Password1!")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            when(memberRepository.existsByEmail(command.getEmail())).thenReturn(false);
            when(memberRepository.existsByNicknameIgnoreCase(command.getNickname())).thenReturn(false);
            when(emailVerificationRepository.findByEmailAndVerified(command.getEmail(), true))
                    .thenReturn(Optional.of(mock(EmailVerification.class)));
            when(passwordEncoder.encode(command.getPassword())).thenReturn("encodedPassword");
            when(memberRepository.save(any(Member.class))).thenAnswer(i -> {
                Member m = i.getArgument(0);
                ReflectionTestUtils.setField(m, "id", 1L);
                return m;
            });
            when(tokenProvider.createAccessToken(anyLong(), anyString(), any())).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(anyLong())).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            LoginResult result = authService.signup(command);

            // then
            assertThat(result.getMember().getId()).isEqualTo(1L);
            assertThat(result.getMember().getEmail()).isEqualTo("user@example.com");
            assertThat(result.getMember().getName()).isEqualTo("홍길동");
            assertThat(result.getMember().getNickname()).isEqualTo("길동이");
            assertThat(result.getMember().getRole()).isEqualTo(Role.USER);
            assertThat(result.isFirstLogin()).isTrue();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");

            verify(memberRepository).existsByEmail(command.getEmail());
            verify(emailVerificationRepository).findByEmailAndVerified(command.getEmail(), true);
            verify(passwordValidator).validate(command.getPassword());
            verify(passwordEncoder).encode(command.getPassword());
            verify(memberRepository).save(any(Member.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("이미 가입된 이메일로 회원가입 시 실패한다")
        void fail_duplicateEmail() {
            // given
            SignupCommand command = SignupCommand.builder()
                    .email("user@example.com")
                    .password("Password1!")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            when(memberRepository.existsByEmail(command.getEmail())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(command))
                    .isInstanceOf(DuplicateEmailException.class);

            verify(memberRepository).existsByEmail(command.getEmail());
            verify(emailVerificationRepository, never()).findByEmailAndVerified(anyString(), anyBoolean());
            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("이메일 인증 없이 회원가입 시 실패한다")
        void fail_emailNotVerified() {
            // given
            SignupCommand command = SignupCommand.builder()
                    .email("user@example.com")
                    .password("Password1!")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            when(memberRepository.existsByEmail(command.getEmail())).thenReturn(false);
            when(emailVerificationRepository.findByEmailAndVerified(command.getEmail(), true))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.signup(command))
                    .isInstanceOf(EmailNotVerifiedException.class);

            verify(memberRepository).existsByEmail(command.getEmail());
            verify(emailVerificationRepository).findByEmailAndVerified(command.getEmail(), true);
            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("비밀번호 정책 위반 시 회원가입 실패한다")
        void fail_invalidPassword() {
            // given
            SignupCommand command = SignupCommand.builder()
                    .email("user@example.com")
                    .password("short")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            when(memberRepository.existsByEmail(command.getEmail())).thenReturn(false);
            when(emailVerificationRepository.findByEmailAndVerified(command.getEmail(), true))
                    .thenReturn(Optional.of(mock(EmailVerification.class)));

            // when & then
            assertThatThrownBy(() -> authService.signup(command))
                    .isInstanceOf(InvalidPasswordException.class);

            verify(memberRepository).existsByEmail(command.getEmail());
            verify(emailVerificationRepository).findByEmailAndVerified(command.getEmail(), true);
            verify(memberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("기존 회원 로그인 시 isFirstLogin이 false이다")
        void loginReturnsIsFirstLoginFalse() {
            // given
            String email = "user@example.com";
            String password = "Password1!";

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn(email);
            when(member.getPassword()).thenReturn("encodedPassword");
            when(member.getRole()).thenReturn(Role.USER);

            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
            when(tokenProvider.createAccessToken(anyLong(), anyString(), any())).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(anyLong())).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            LoginResult result = authService.login(email, password);

            // then
            assertThat(result.getMember().getId()).isEqualTo(1L);
            assertThat(result.isFirstLogin()).isFalse();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");

            verify(memberRepository).findByEmail(email);
            verify(refreshTokenRepository).deleteByMemberId(1L);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }
}
