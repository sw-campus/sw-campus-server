package com.swcampus.infra.postgres.survey.json;

import com.swcampus.domain.survey.BasicSurvey;
import com.swcampus.domain.survey.BudgetRange;
import com.swcampus.domain.survey.DesiredJob;
import com.swcampus.domain.survey.LearningMethod;
import com.swcampus.domain.survey.MajorInfo;
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
    // 기존 필드 (하위 호환성 유지 - 읽기 전용)
    private String major;

    // 새 필드
    private MajorInfoJson majorInfo;
    private ProgrammingExperienceJson programmingExperience;
    private String preferredLearningMethod;
    private List<String> desiredJobs;
    private String desiredJobOther;
    private String affordableBudgetRange;

    public static BasicSurveyJson from(BasicSurvey domain) {
        if (domain == null) return null;

        BasicSurveyJson json = new BasicSurveyJson();
        json.setMajorInfo(MajorInfoJson.from(domain.getMajorInfo()));
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
        // 하위 호환성: majorInfo가 없으면 기존 major 필드로 변환
        MajorInfo majorInfoDomain;
        if (majorInfo != null) {
            majorInfoDomain = majorInfo.toDomain();
        } else if (major != null && !major.isEmpty()) {
            // 기존 데이터 마이그레이션: major 문자열이 있으면 전공 있음으로 처리
            majorInfoDomain = MajorInfo.withMajor(major);
        } else {
            majorInfoDomain = MajorInfo.noMajor();
        }

        return BasicSurvey.builder()
                .majorInfo(majorInfoDomain)
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
