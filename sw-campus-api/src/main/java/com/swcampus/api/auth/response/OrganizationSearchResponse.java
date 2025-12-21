package com.swcampus.api.auth.response;

import com.swcampus.domain.organization.Organization;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "기관 검색 응답")
public class OrganizationSearchResponse {

    @Schema(description = "기관 ID", example = "1")
    private Long id;

    @Schema(description = "기관명", example = "한국기술사업화진흥협회")
    private String name;

    public static OrganizationSearchResponse from(Organization organization) {
        return OrganizationSearchResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .build();
    }

    public static List<OrganizationSearchResponse> fromList(List<Organization> organizations) {
        return organizations.stream()
                .map(OrganizationSearchResponse::from)
                .toList();
    }
}
