package com.swcampus.domain.organization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApproveOrganizationResult {
    private final Organization organization;
    private final String memberEmail;
}
