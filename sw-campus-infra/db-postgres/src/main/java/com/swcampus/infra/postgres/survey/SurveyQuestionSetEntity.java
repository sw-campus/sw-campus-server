package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.QuestionSetStatus;
import com.swcampus.domain.survey.QuestionSetType;
import com.swcampus.domain.survey.SurveyQuestion;
import com.swcampus.domain.survey.SurveyQuestionSet;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "survey_question_sets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyQuestionSetEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_set_id")
    private Long questionSetId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private QuestionSetType type;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuestionSetStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_set_id")
    @OrderBy("questionOrder ASC")
    private Set<SurveyQuestionEntity> questions = new HashSet<>();

    public static SurveyQuestionSetEntity from(SurveyQuestionSet questionSet) {
        SurveyQuestionSetEntity entity = new SurveyQuestionSetEntity();
        entity.questionSetId = questionSet.getQuestionSetId();
        entity.name = questionSet.getName();
        entity.description = questionSet.getDescription();
        entity.type = questionSet.getType();
        entity.version = questionSet.getVersion();
        entity.status = questionSet.getStatus();
        entity.publishedAt = questionSet.getPublishedAt();

        if (questionSet.getQuestions() != null) {
            entity.questions = questionSet.getQuestions().stream()
                    .map(q -> SurveyQuestionEntity.from(q, questionSet.getQuestionSetId()))
                    .collect(Collectors.toSet());
        }
        return entity;
    }

    public SurveyQuestionSet toDomain() {
        List<SurveyQuestion> domainQuestions = this.questions != null
                ? this.questions.stream()
                    .sorted((a, b) -> Integer.compare(a.getQuestionOrder(), b.getQuestionOrder()))
                    .map(SurveyQuestionEntity::toDomain)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return SurveyQuestionSet.builder()
                .questionSetId(this.questionSetId)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .version(this.version)
                .status(this.status)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .publishedAt(this.publishedAt)
                .questions(domainQuestions)
                .build();
    }

    public SurveyQuestionSet toDomainWithoutQuestions() {
        return SurveyQuestionSet.builder()
                .questionSetId(this.questionSetId)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .version(this.version)
                .status(this.status)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .publishedAt(this.publishedAt)
                .build();
    }

    public void update(SurveyQuestionSet questionSet) {
        this.name = questionSet.getName();
        this.description = questionSet.getDescription();
        this.status = questionSet.getStatus();
        this.publishedAt = questionSet.getPublishedAt();
    }
}
