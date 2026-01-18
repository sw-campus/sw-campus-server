package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyQuestion {
    private Long questionId;
    private Long questionSetId;
    private Integer questionOrder;
    private String questionText;
    private QuestionType questionType;
    private boolean isRequired;
    private String fieldKey;
    private Long parentQuestionId;
    private Map<String, Object> showCondition;
    private Map<String, Object> metadata;
    private QuestionPart part;
    private LocalDateTime createdAt;
    private List<SurveyOption> options = new ArrayList<>();

    @Builder
    public SurveyQuestion(
            Long questionId,
            Long questionSetId,
            Integer questionOrder,
            String questionText,
            QuestionType questionType,
            boolean isRequired,
            String fieldKey,
            Long parentQuestionId,
            Map<String, Object> showCondition,
            Map<String, Object> metadata,
            QuestionPart part,
            LocalDateTime createdAt,
            List<SurveyOption> options
    ) {
        this.questionId = questionId;
        this.questionSetId = questionSetId;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.questionType = questionType;
        this.isRequired = isRequired;
        this.fieldKey = fieldKey;
        this.parentQuestionId = parentQuestionId;
        this.showCondition = showCondition;
        this.metadata = metadata;
        this.part = part;
        this.createdAt = createdAt;
        this.options = options != null ? options : new ArrayList<>();
    }

    public void addOption(SurveyOption option) {
        this.options.add(option);
    }

    public void update(
            String questionText,
            QuestionType questionType,
            boolean isRequired,
            String fieldKey,
            Map<String, Object> showCondition,
            Map<String, Object> metadata,
            QuestionPart part
    ) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.isRequired = isRequired;
        this.fieldKey = fieldKey;
        this.showCondition = showCondition;
        this.metadata = metadata;
        this.part = part;
    }

    public void updateOrder(int newOrder) {
        this.questionOrder = newOrder;
    }
}
