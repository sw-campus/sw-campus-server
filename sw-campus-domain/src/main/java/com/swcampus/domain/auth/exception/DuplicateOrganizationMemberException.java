package com.swcampus.domain.auth.exception;

public class DuplicateOrganizationMemberException extends RuntimeException {

    public DuplicateOrganizationMemberException() {
        super("이미 다른 사용자가 연결된 기관입니다");
    }

    public DuplicateOrganizationMemberException(Long organizationId) {
        super(String.format("이미 다른 사용자가 연결된 기관입니다: %d", organizationId));
    }
}
