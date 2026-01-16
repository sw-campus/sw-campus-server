package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BasicSurvey {
    private MajorInfo majorInfo;
    private ProgrammingExperience programmingExperience;
    private LearningMethod preferredLearningMethod;
    private List<DesiredJob> desiredJobs;
    private String desiredJobOther;
    private BudgetRange affordableBudgetRange;

    @Builder
    public BasicSurvey(
            MajorInfo majorInfo,
            ProgrammingExperience programmingExperience,
            LearningMethod preferredLearningMethod,
            List<DesiredJob> desiredJobs,
            String desiredJobOther,
            BudgetRange affordableBudgetRange
    ) {
        this.majorInfo = majorInfo;
        this.programmingExperience = programmingExperience;
        this.preferredLearningMethod = preferredLearningMethod;
        this.desiredJobs = desiredJobs;
        this.desiredJobOther = desiredJobOther;
        this.affordableBudgetRange = affordableBudgetRange;
    }
}
