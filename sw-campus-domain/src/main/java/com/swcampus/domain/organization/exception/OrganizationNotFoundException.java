package com.swcampus.domain.organization.exception;

import com.swcampus.domain.common.ResourceNotFoundException;

public class OrganizationNotFoundException extends ResourceNotFoundException {
    public OrganizationNotFoundException(Long id) {
        super("기관을 찾을 수 없습니다. id: " + id);
    }
}
