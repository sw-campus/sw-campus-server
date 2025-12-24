package com.swcampus.api.admin.response;

import com.swcampus.domain.testdata.TestDataCreateResult;

import java.util.List;
import java.util.Map;

public record TestDataCreateResponse(
        String batchId,
        CreatedData created,
        int totalCount
) {
    public record CreatedData(
            List<Long> organizations,
            List<Long> lectures,
            List<Long> members,
            List<Long> certificates,
            List<Long> reviews,
            List<Long> surveys
    ) {}

    public static TestDataCreateResponse from(TestDataCreateResult result) {
        return new TestDataCreateResponse(
                result.getBatchId(),
                new CreatedData(
                        result.getOrganizationIds(),
                        result.getLectureIds(),
                        result.getMemberIds(),
                        result.getCertificateIds(),
                        result.getReviewIds(),
                        result.getSurveyMemberIds()
                ),
                result.getTotalCount()
        );
    }
}
