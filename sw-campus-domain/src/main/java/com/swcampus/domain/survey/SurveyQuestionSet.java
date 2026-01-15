package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyQuestionSet {
    private Long questionSetId;
    private String name;
    private String description;
    private QuestionSetType type;
    private Integer version;
    private QuestionSetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private List<SurveyQuestion> questions = new ArrayList<>();

    @Builder
    public SurveyQuestionSet(
            Long questionSetId,
            String name,
            String description,
            QuestionSetType type,
            Integer version,
            QuestionSetStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime publishedAt,
            List<SurveyQuestion> questions
    ) {
        this.questionSetId = questionSetId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.version = version != null ? version : 1;
        this.status = status != null ? status : QuestionSetStatus.DRAFT;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publishedAt = publishedAt;
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public static SurveyQuestionSet createDraft(String name, String description, QuestionSetType type) {
        return SurveyQuestionSet.builder()
                .name(name)
                .description(description)
                .type(type)
                .version(1)
                .status(QuestionSetStatus.DRAFT)
                .build();
    }

    public void update(String name, String description) {
        if (!status.isEditable()) {
            throw new IllegalStateException("발행된 문항 세트는 수정할 수 없습니다.");
        }
        this.name = name;
        this.description = description;
    }

    public void publish() {
        if (this.status != QuestionSetStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 발행할 수 있습니다.");
        }
        this.status = QuestionSetStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        if (this.status != QuestionSetStatus.PUBLISHED) {
            throw new IllegalStateException("PUBLISHED 상태에서만 보관할 수 있습니다.");
        }
        this.status = QuestionSetStatus.ARCHIVED;
    }

    public void addQuestion(SurveyQuestion question) {
        if (!status.isEditable()) {
            throw new IllegalStateException("발행된 문항 세트에는 문항을 추가할 수 없습니다.");
        }
        this.questions.add(question);
    }

    public boolean isEditable() {
        return status.isEditable();
    }

    /**
     * 새 버전용 복제본 생성
     */
    public SurveyQuestionSet cloneForNewVersion(int newVersion) {
        return SurveyQuestionSet.builder()
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .version(newVersion)
                .status(QuestionSetStatus.DRAFT)
                .build();
    }
}
