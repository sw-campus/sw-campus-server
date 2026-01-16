package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.JobTypeCode;
import com.swcampus.domain.survey.SurveyOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "survey_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "option_text", nullable = false, length = 200)
    private String optionText;

    @Column(name = "option_value", length = 100)
    private String optionValue;

    @Column(name = "score")
    private Integer score;

    @Column(name = "job_type", length = 10)
    @Enumerated(EnumType.STRING)
    private JobTypeCode jobType;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    public static SurveyOptionEntity from(SurveyOption option, Long questionId) {
        SurveyOptionEntity entity = new SurveyOptionEntity();
        entity.optionId = option.getOptionId();
        entity.questionId = questionId;
        entity.optionOrder = option.getOptionOrder();
        entity.optionText = option.getOptionText();
        entity.optionValue = option.getOptionValue();
        entity.score = option.getScore();
        entity.jobType = option.getJobType();
        entity.isCorrect = option.getIsCorrect();
        return entity;
    }

    public SurveyOption toDomain() {
        return SurveyOption.builder()
                .optionId(this.optionId)
                .questionId(this.questionId)
                .optionOrder(this.optionOrder)
                .optionText(this.optionText)
                .optionValue(this.optionValue)
                .score(this.score)
                .jobType(this.jobType)
                .isCorrect(this.isCorrect)
                .build();
    }

    public void update(SurveyOption option) {
        this.optionOrder = option.getOptionOrder();
        this.optionText = option.getOptionText();
        this.optionValue = option.getOptionValue();
        this.score = option.getScore();
        this.jobType = option.getJobType();
        this.isCorrect = option.getIsCorrect();
    }
}
