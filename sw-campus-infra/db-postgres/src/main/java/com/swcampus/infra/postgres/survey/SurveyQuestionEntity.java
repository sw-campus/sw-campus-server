package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.QuestionPart;
import com.swcampus.domain.survey.QuestionType;
import com.swcampus.domain.survey.SurveyOption;
import com.swcampus.domain.survey.SurveyQuestion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "survey_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_set_id", nullable = false)
    private Long questionSetId;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "field_key", length = 50)
    private String fieldKey;

    @Column(name = "parent_question_id")
    private Long parentQuestionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "show_condition", columnDefinition = "jsonb")
    private Map<String, Object> showCondition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "part", length = 20)
    @Enumerated(EnumType.STRING)
    private QuestionPart part;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    @OrderBy("optionOrder ASC")
    private Set<SurveyOptionEntity> options = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static SurveyQuestionEntity from(SurveyQuestion question, Long questionSetId) {
        SurveyQuestionEntity entity = new SurveyQuestionEntity();
        entity.questionId = question.getQuestionId();
        entity.questionSetId = questionSetId;
        entity.questionOrder = question.getQuestionOrder();
        entity.questionText = question.getQuestionText();
        entity.questionType = question.getQuestionType();
        entity.isRequired = question.isRequired();
        entity.fieldKey = question.getFieldKey();
        entity.parentQuestionId = question.getParentQuestionId();
        entity.showCondition = question.getShowCondition();
        entity.metadata = question.getMetadata();
        entity.part = question.getPart();
        entity.createdAt = question.getCreatedAt();

        if (question.getOptions() != null) {
            entity.options = question.getOptions().stream()
                    .map(opt -> SurveyOptionEntity.from(opt, question.getQuestionId()))
                    .collect(Collectors.toSet());
        }
        return entity;
    }

    public SurveyQuestion toDomain() {
        List<SurveyOption> domainOptions = this.options != null
                ? this.options.stream()
                    .sorted((a, b) -> Integer.compare(a.getOptionOrder(), b.getOptionOrder()))
                    .map(SurveyOptionEntity::toDomain)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return SurveyQuestion.builder()
                .questionId(this.questionId)
                .questionSetId(this.questionSetId)
                .questionOrder(this.questionOrder)
                .questionText(this.questionText)
                .questionType(this.questionType)
                .isRequired(this.isRequired)
                .fieldKey(this.fieldKey)
                .parentQuestionId(this.parentQuestionId)
                .showCondition(this.showCondition)
                .metadata(this.metadata)
                .part(this.part)
                .createdAt(this.createdAt)
                .options(domainOptions)
                .build();
    }

    public void update(SurveyQuestion question) {
        this.questionOrder = question.getQuestionOrder();
        this.questionText = question.getQuestionText();
        this.questionType = question.getQuestionType();
        this.isRequired = question.isRequired();
        this.fieldKey = question.getFieldKey();
        this.showCondition = question.getShowCondition();
        this.metadata = question.getMetadata();
        this.part = question.getPart();
    }
}
