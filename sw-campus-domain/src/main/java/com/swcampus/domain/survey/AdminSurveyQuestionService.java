package com.swcampus.domain.survey;

import com.swcampus.domain.survey.exception.QuestionSetNotEditableException;
import com.swcampus.domain.survey.exception.SurveyQuestionSetNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSurveyQuestionService {

    private final SurveyQuestionSetRepository questionSetRepository;

    /**
     * 새 문항 세트 생성 (DRAFT 상태)
     */
    @Transactional
    public SurveyQuestionSet createQuestionSet(String name, String description, QuestionSetType type) {
        SurveyQuestionSet questionSet = SurveyQuestionSet.createDraft(name, description, type);
        return questionSetRepository.save(questionSet);
    }

    /**
     * 문항 세트 조회
     */
    public SurveyQuestionSet getQuestionSet(Long questionSetId) {
        return questionSetRepository.findById(questionSetId)
                .orElseThrow(SurveyQuestionSetNotFoundException::new);
    }

    /**
     * 문항 세트 상세 조회 (문항 포함)
     */
    public SurveyQuestionSet getQuestionSetWithQuestions(Long questionSetId) {
        return questionSetRepository.findByIdWithQuestions(questionSetId)
                .orElseThrow(SurveyQuestionSetNotFoundException::new);
    }

    /**
     * 특정 타입의 모든 문항 세트 조회
     */
    public List<SurveyQuestionSet> getQuestionSetsByType(QuestionSetType type) {
        return questionSetRepository.findAllByType(type);
    }

    /**
     * 문항 세트 수정 (DRAFT 상태만 가능)
     */
    @Transactional
    public SurveyQuestionSet updateQuestionSet(Long questionSetId, String name, String description) {
        SurveyQuestionSet questionSet = getQuestionSet(questionSetId);
        questionSet.update(name, description);
        return questionSetRepository.save(questionSet);
    }

    /**
     * 문항 세트 삭제 (DRAFT 상태만 가능)
     */
    @Transactional
    public void deleteQuestionSet(Long questionSetId) {
        SurveyQuestionSet questionSet = getQuestionSet(questionSetId);
        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(questionSetId);
        }
        questionSetRepository.delete(questionSet);
    }

    /**
     * 문항 세트 발행
     * - 기존 PUBLISHED 세트는 ARCHIVED로 변경
     * - 현재 세트를 PUBLISHED로 변경
     */
    @Transactional
    public SurveyQuestionSet publishQuestionSet(Long questionSetId) {
        SurveyQuestionSet questionSet = getQuestionSet(questionSetId);

        // 기존 PUBLISHED → ARCHIVED
        questionSetRepository.archivePublishedByType(questionSet.getType());

        // 현재 세트 발행
        questionSet.publish();
        return questionSetRepository.save(questionSet);
    }

    /**
     * 기존 세트를 복제하여 새 버전 생성
     */
    @Transactional
    public SurveyQuestionSet cloneQuestionSet(Long questionSetId) {
        SurveyQuestionSet original = getQuestionSetWithQuestions(questionSetId);

        int newVersion = questionSetRepository.findMaxVersionByType(original.getType()) + 1;
        SurveyQuestionSet clone = original.cloneForNewVersion(newVersion);

        return questionSetRepository.save(clone);
    }

    /**
     * PUBLISHED 상태의 문항 세트 조회 (사용자 API용)
     */
    public SurveyQuestionSet getPublishedQuestionSet(QuestionSetType type) {
        return questionSetRepository.findPublishedByTypeWithQuestions(type)
                .orElseThrow(() -> new SurveyQuestionSetNotFoundException(type.name()));
    }
}
