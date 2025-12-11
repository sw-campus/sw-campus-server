package com.swcampus.domain.organization.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizationSearchCondition {
    private String keyword; // 업체명 검색
}
