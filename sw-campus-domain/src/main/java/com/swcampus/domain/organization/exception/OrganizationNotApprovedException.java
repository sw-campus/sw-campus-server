package com.swcampus.domain.organization.exception;

public class OrganizationNotApprovedException extends RuntimeException {

    public OrganizationNotApprovedException() {
        super("기관 승인이 완료되지 않아 해당 기능을 사용할 수 없습니다");
    }

    public OrganizationNotApprovedException(String message) {
        super(message);
    }
}
