package com.swcampus.domain.member;

import com.swcampus.domain.member.exception.MemberNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("일반 사용자 비밀번호 검증 성공")
    void validatePassword_success() {
        // given
        Long memberId = 1L;
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";
        
        Member member = Member.createUser("test@example.com", encodedPassword, "name", "nickname", "010-1234-5678", "Seoul");
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

        // when
        boolean result = memberService.validatePassword(memberId, rawPassword);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("일반 사용자 비밀번호 검증 실패 - 비밀번호 불일치")
    void validatePassword_fail_wrongPassword() {
        // given
        Long memberId = 1L;
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        Member member = Member.createUser("test@example.com", encodedPassword, "name", "nickname", "010-1234-5678", "Seoul");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

        // when
        boolean result = memberService.validatePassword(memberId, rawPassword);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("OAuth 사용자 비밀번호 검증 - 항상 성공(true)")
    void validatePassword_oauthUser_alwaysTrue() {
        // given
        Long memberId = 1L;
        String rawPassword = "anyPassword"; // OAuth 유저는 입력값 무시

        Member member = Member.createOAuthUser("oauth@example.com", "OAuth User", "행복한고양이구름");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        boolean result = memberService.validatePassword(memberId, rawPassword);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("일반 가입 후 OAuth 연동한 사용자 - 비밀번호가 존재하므로 검증 수행")
    void validatePassword_linkedUser_checkPassword() {
        // given
        Long memberId = 1L;
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        // 일반 가입으로 생성 (비밀번호 있음)
        Member member = Member.createUser("test@example.com", encodedPassword, "name", "nickname", "010-1234-5678", "Seoul");
        // (참고: 실제 DB에는 SocialAccount가 연결되어 있겠지만, Member 객체 자체는 비밀번호를 가지고 있음)

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

        // when
        boolean result = memberService.validatePassword(memberId, rawPassword);

        // then
        assertThat(result).isTrue();
        // 비밀번호 검증 로직이 실제로 호출되었는지 확인
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 검증 시 예외 발생")
    void validatePassword_memberNotFound() {
        // given
        Long memberId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.validatePassword(memberId, "password"))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
