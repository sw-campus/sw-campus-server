package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PasswordValidator 테스트")
class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    @DisplayName("유효한 비밀번호는 검증을 통과한다")
    void validPassword() {
        // given
        String password = "Password1!";

        // when & then
        assertThatNoException().isThrownBy(() -> validator.validate(password));
    }

    @Test
    @DisplayName("8자 이상 특수문자 포함 비밀번호는 통과한다")
    void validPasswordWithMinLength() {
        // given
        String password = "Passwor!";

        // when & then
        assertThatNoException().isThrownBy(() -> validator.validate(password));
    }

    @Test
    @DisplayName("8자 미만 비밀번호는 검증에 실패한다")
    void tooShortPassword() {
        // given
        String password = "Pass1!";

        // when & then
        assertThatThrownBy(() -> validator.validate(password))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("8자 이상");
    }

    @Test
    @DisplayName("특수문자가 없는 비밀번호는 검증에 실패한다")
    void noSpecialCharacter() {
        // given
        String password = "Password1";

        // when & then
        assertThatThrownBy(() -> validator.validate(password))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("특수문자");
    }

    @Test
    @DisplayName("null 비밀번호는 검증에 실패한다")
    void nullPassword() {
        // when & then
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("빈 문자열 비밀번호는 검증에 실패한다")
    void emptyPassword() {
        // given
        String password = "";

        // when & then
        assertThatThrownBy(() -> validator.validate(password))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
