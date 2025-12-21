package com.swcampus.domain.organization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RejectOrganizationResult {
    private final String memberEmail;
    private final String adminEmail;
    private final String adminPhone;
}
