package com.swcampus.infra.postgres.survey.json;

import com.swcampus.domain.survey.BasicSurvey;
import com.swcampus.domain.survey.BudgetRange;
import com.swcampus.domain.survey.DesiredJob;
import com.swcampus.domain.survey.LearningMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasicSurveyJson {
    private String major;
    private ProgrammingExperienceJson programmingExperience;
    private String preferredLearningMethod;
    private List<String> desiredJobs;
    private String desiredJobOther;
    private String affordableBudgetRange;

    public static BasicSurveyJson from(BasicSurvey domain) {
        if (domain == null) return null;

        BasicSurveyJson json = new BasicSurveyJson();
        json.setMajor(domain.getMajor());
        json.setProgrammingExperience(ProgrammingExperienceJson.from(domain.getProgrammingExperience()));
        json.setPreferredLearningMethod(domain.getPreferredLearningMethod() != null
                ? domain.getPreferredLearningMethod().name() : null);
        json.setDesiredJobs(domain.getDesiredJobs() != null
                ? domain.getDesiredJobs().stream().map(Enum::name).collect(Collectors.toList()) : null);
        json.setDesiredJobOther(domain.getDesiredJobOther());
        json.setAffordableBudgetRange(domain.getAffordableBudgetRange() != null
                ? domain.getAffordableBudgetRange().name() : null);
        return json;
    }

    public BasicSurvey toDomain() {
        return BasicSurvey.builder()
                .major(major)
                .programmingExperience(programmingExperience != null ? programmingExperience.toDomain() : null)
                .preferredLearningMethod(preferredLearningMethod != null
                        ? LearningMethod.valueOf(preferredLearningMethod) : null)
                .desiredJobs(desiredJobs != null
                        ? desiredJobs.stream().map(DesiredJob::valueOf).collect(Collectors.toList()) : null)
                .desiredJobOther(desiredJobOther)
                .affordableBudgetRange(affordableBudgetRange != null
                        ? BudgetRange.valueOf(affordableBudgetRange) : null)
                .build();
    }
}
