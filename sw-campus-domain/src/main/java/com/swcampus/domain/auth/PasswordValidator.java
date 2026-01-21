package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.InvalidPasswordException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new InvalidPasswordException("비밀번호는 " + MIN_LENGTH + "자 이상이어야 합니다");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new InvalidPasswordException("비밀번호에 대문자가 1개 이상 포함되어야 합니다");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new InvalidPasswordException("비밀번호에 소문자가 1개 이상 포함되어야 합니다");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new InvalidPasswordException("비밀번호에 숫자가 1개 이상 포함되어야 합니다");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            throw new InvalidPasswordException("비밀번호에 특수문자가 1개 이상 포함되어야 합니다");
        }
    }
}
