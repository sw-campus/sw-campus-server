package com.swcampus.domain.testdata;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TestDataCreateResult {
    private final String batchId;
    private final List<Long> organizationIds;
    private final List<Long> lectureIds;
    private final List<Long> memberIds;
    private final List<Long> certificateIds;
    private final List<Long> reviewIds;
    private final List<Long> surveyMemberIds;

    public int getTotalCount() {
        int count = 0;
        if (organizationIds != null) count += organizationIds.size();
        if (lectureIds != null) count += lectureIds.size();
        if (memberIds != null) count += memberIds.size();
        if (certificateIds != null) count += certificateIds.size();
        if (reviewIds != null) count += reviewIds.size();
        if (surveyMemberIds != null) count += surveyMemberIds.size();
        return count;
    }
}
