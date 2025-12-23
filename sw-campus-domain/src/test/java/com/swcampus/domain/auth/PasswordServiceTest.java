package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.InvalidPasswordException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordService 테스트")
class PasswordServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword() {
        // given
        Long userId = 1L;
        String currentPassword = "OldPassword1!";
        String newPassword = "NewPassword1!";

        Member member = mock(Member.class);
        when(member.getPassword()).thenReturn("encodedOldPassword");

        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(currentPassword, "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // when
        passwordService.changePassword(userId, currentPassword, newPassword);

        // then
        verify(passwordValidator).validate(newPassword);
        verify(member).changePassword("encodedNewPassword");
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 변경 실패")
    void changePassword_wrongCurrentPassword() {
        // given
        Long userId = 1L;

        Member member = mock(Member.class);
        when(member.getPassword()).thenReturn("encodedOldPassword");

        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
                passwordService.changePassword(userId, "wrongPassword", "NewPassword1!"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("현재 비밀번호");
    }

    @Test
    @DisplayName("OAuth 사용자는 비밀번호 변경 불가")
    void changePassword_oauthUser() {
        // given
        Long userId = 1L;

        Member member = mock(Member.class);
        when(member.getPassword()).thenReturn(null);  // OAuth 사용자

        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() ->
                passwordService.changePassword(userId, "anyPassword", "NewPassword1!"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("소셜 로그인");
    }

    @Test
    @DisplayName("임시 비밀번호를 발급할 수 있다")
    void issueTemporaryPassword() {
        // given
        String email = "user@example.com";

        Member member = mock(Member.class);
        when(member.getPassword()).thenReturn("existingPassword");  // 일반 가입자

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");

        // when
        passwordService.issueTemporaryPassword(email);

        // then
        verify(member).changePassword("encodedTempPassword");
        verify(memberRepository).save(member);
        verify(mailSender).send(eq(email), anyString(), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 이메일에도 동일 응답 (보안)")
    void issueTemporaryPassword_notFoundEmail() {
        // given
        String email = "notfound@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then (예외 없이 정상 종료)
        assertThatCode(() -> passwordService.issueTemporaryPassword(email))
                .doesNotThrowAnyException();

        verify(mailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("OAuth 사용자에게는 임시 비밀번호 미발급 (보안)")
    void issueTemporaryPassword_oauthUser() {
        // given
        String email = "oauth@example.com";

        Member member = mock(Member.class);
        when(member.getPassword()).thenReturn(null);  // OAuth 사용자

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when & then (예외 없이 정상 종료, 메일 미발송)
        assertThatCode(() -> passwordService.issueTemporaryPassword(email))
                .doesNotThrowAnyException();

        verify(mailSender, never()).send(anyString(), anyString(), anyString());
    }
}
