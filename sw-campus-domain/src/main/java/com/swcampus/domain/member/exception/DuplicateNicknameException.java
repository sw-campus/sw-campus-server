package com.swcampus.domain.member.exception;

public class DuplicateNicknameException extends RuntimeException {

    public DuplicateNicknameException() {
        super("이미 사용 중인 닉네임입니다");
    }

    public DuplicateNicknameException(String nickname) {
        super(String.format("이미 사용 중인 닉네임입니다: %s", nickname));
    }
}
