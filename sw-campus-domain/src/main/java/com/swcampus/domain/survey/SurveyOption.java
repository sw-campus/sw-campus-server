package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyOption {
    private Long optionId;
    private Long questionId;
    private Integer optionOrder;
    private String optionText;
    private String optionValue;
    private Integer score;
    private JobTypeCode jobType;
    private Boolean isCorrect;

    @Builder
    public SurveyOption(
            Long optionId,
            Long questionId,
            Integer optionOrder,
            String optionText,
            String optionValue,
            Integer score,
            JobTypeCode jobType,
            Boolean isCorrect
    ) {
        this.optionId = optionId;
        this.questionId = questionId;
        this.optionOrder = optionOrder;
        this.optionText = optionText;
        this.optionValue = optionValue;
        this.score = score != null ? score : 0;
        this.jobType = jobType;
        this.isCorrect = isCorrect != null ? isCorrect : false;
    }

    public void update(String optionText, String optionValue, Integer score, JobTypeCode jobType, Boolean isCorrect) {
        this.optionText = optionText;
        this.optionValue = optionValue;
        this.score = score != null ? score : 0;
        this.jobType = jobType;
        this.isCorrect = isCorrect != null ? isCorrect : false;
    }

    public void updateOrder(int newOrder) {
        this.optionOrder = newOrder;
    }
}
