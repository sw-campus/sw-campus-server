package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.*;
import com.swcampus.infra.postgres.BaseEntity;
import com.swcampus.infra.postgres.survey.json.AptitudeTestJson;
import com.swcampus.infra.postgres.survey.json.BasicSurveyJson;
import com.swcampus.infra.postgres.survey.json.SurveyResultsJson;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_surveys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSurveyEntity extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long memberId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "basic_survey", columnDefinition = "jsonb")
    private BasicSurveyJson basicSurvey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "aptitude_test", columnDefinition = "jsonb")
    private AptitudeTestJson aptitudeTest;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results", columnDefinition = "jsonb")
    private SurveyResultsJson results;

    @Column(name = "aptitude_grade", length = 20)
    @Enumerated(EnumType.STRING)
    private AptitudeGrade aptitudeGrade;

    @Column(name = "recommended_job", length = 20)
    @Enumerated(EnumType.STRING)
    private RecommendedJob recommendedJob;

    @Column(name = "aptitude_score")
    private Integer aptitudeScore;

    @Column(name = "question_set_version")
    private Integer questionSetVersion;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static MemberSurveyEntity from(MemberSurvey survey) {
        MemberSurveyEntity entity = new MemberSurveyEntity();
        entity.memberId = survey.getMemberId();
        entity.basicSurvey = BasicSurveyJson.from(survey.getBasicSurvey());
        entity.aptitudeTest = AptitudeTestJson.from(survey.getAptitudeTest());
        entity.results = SurveyResultsJson.from(survey.getResults());
        entity.aptitudeGrade = survey.getAptitudeGrade();
        entity.recommendedJob = survey.getRecommendedJob();
        entity.aptitudeScore = survey.getAptitudeScore();
        entity.questionSetVersion = survey.getQuestionSetVersion();
        entity.completedAt = survey.getCompletedAt();
        return entity;
    }

    public MemberSurvey toDomain() {
        return MemberSurvey.builder()
                .memberId(this.memberId)
                .basicSurvey(this.basicSurvey != null ? this.basicSurvey.toDomain() : null)
                .aptitudeTest(this.aptitudeTest != null ? this.aptitudeTest.toDomain() : null)
                .results(this.results != null ? this.results.toDomain() : null)
                .aptitudeGrade(this.aptitudeGrade)
                .recommendedJob(this.recommendedJob)
                .aptitudeScore(this.aptitudeScore)
                .questionSetVersion(this.questionSetVersion)
                .completedAt(this.completedAt)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    public void update(MemberSurvey survey) {
        this.basicSurvey = BasicSurveyJson.from(survey.getBasicSurvey());
        this.aptitudeTest = AptitudeTestJson.from(survey.getAptitudeTest());
        this.results = SurveyResultsJson.from(survey.getResults());
        this.aptitudeGrade = survey.getAptitudeGrade();
        this.recommendedJob = survey.getRecommendedJob();
        this.aptitudeScore = survey.getAptitudeScore();
        this.questionSetVersion = survey.getQuestionSetVersion();
        this.completedAt = survey.getCompletedAt();
    }
}
