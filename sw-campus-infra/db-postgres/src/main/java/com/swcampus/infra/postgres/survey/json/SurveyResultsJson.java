package com.swcampus.infra.postgres.survey.json;

import com.swcampus.domain.survey.AptitudeGrade;
import com.swcampus.domain.survey.JobTypeCode;
import com.swcampus.domain.survey.RecommendedJob;
import com.swcampus.domain.survey.SurveyResults;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResultsJson {
    private Integer aptitudeScore;
    private String aptitudeGrade;
    private Map<String, Integer> jobTypeScores;
    private String recommendedJob;

    public static SurveyResultsJson from(SurveyResults domain) {
        if (domain == null) return null;

        SurveyResultsJson json = new SurveyResultsJson();
        json.setAptitudeScore(domain.getAptitudeScore());
        json.setAptitudeGrade(domain.getAptitudeGrade() != null ? domain.getAptitudeGrade().name() : null);
        json.setJobTypeScores(domain.getJobTypeScores() != null
                ? domain.getJobTypeScores().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue))
                : null);
        json.setRecommendedJob(domain.getRecommendedJob() != null ? domain.getRecommendedJob().name() : null);
        return json;
    }

    public SurveyResults toDomain() {
        Map<JobTypeCode, Integer> jobScores = null;
        if (jobTypeScores != null) {
            jobScores = new EnumMap<>(JobTypeCode.class);
            for (Map.Entry<String, Integer> entry : jobTypeScores.entrySet()) {
                jobScores.put(JobTypeCode.valueOf(entry.getKey()), entry.getValue());
            }
        }

        return SurveyResults.builder()
                .aptitudeScore(aptitudeScore)
                .aptitudeGrade(aptitudeGrade != null ? AptitudeGrade.valueOf(aptitudeGrade) : null)
                .jobTypeScores(jobScores)
                .recommendedJob(recommendedJob != null ? RecommendedJob.valueOf(recommendedJob) : null)
                .build();
    }
}
