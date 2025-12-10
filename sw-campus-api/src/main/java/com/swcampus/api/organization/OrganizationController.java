package com.swcampus.api.organization;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.lecture.response.LectureResponse;
import com.swcampus.api.organization.response.OrganizationResponse;
import com.swcampus.api.organization.response.OrganizationSummaryResponse;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.organization.dto.OrganizationSearchCondition;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final LectureService lectureService;

    @GetMapping
    public ResponseEntity<List<OrganizationSummaryResponse>> getAllOrganizations(@RequestParam(required = false) String keyword) {
        Map<Long, Long> counts = lectureService.getRecruitingLectureCounts();
        List<Organization> result;
        
        if (keyword != null && !keyword.isBlank()) {
            OrganizationSearchCondition condition = OrganizationSearchCondition.builder()
                .keyword(keyword)
                .build();
            result = organizationService.searchOrganizations(condition);
        } else {
            result = organizationService.getAllOrganizations();
        }

        List<OrganizationSummaryResponse> organizations = result.stream()
                .map(org -> OrganizationSummaryResponse.from(org, counts.getOrDefault(org.getId(), 0L)))
                .toList();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable Long organizationId) {
        Organization organization = organizationService.getOrganization(organizationId);
        return ResponseEntity.ok(OrganizationResponse.from(organization));
    }

    @GetMapping("/{organizationId}/lectures")
    public ResponseEntity<List<LectureResponse>> getOrganizationLectures(@PathVariable Long organizationId) {
        List<LectureResponse> lectures = lectureService.getLecturesByOrgId(organizationId).stream()
            .map(LectureResponse::from)
            .toList();
        return ResponseEntity.ok(lectures);
    }
}
