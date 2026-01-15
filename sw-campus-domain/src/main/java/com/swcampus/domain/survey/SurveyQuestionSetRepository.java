package com.swcampus.domain.survey;

import java.util.List;
import java.util.Optional;

public interface SurveyQuestionSetRepository {

    SurveyQuestionSet save(SurveyQuestionSet questionSet);

    Optional<SurveyQuestionSet> findById(Long questionSetId);

    Optional<SurveyQuestionSet> findByIdWithQuestions(Long questionSetId);

    /**
     * 특정 타입의 PUBLISHED 상태인 문항 세트 조회
     */
    Optional<SurveyQuestionSet> findPublishedByType(QuestionSetType type);

    /**
     * 특정 타입의 PUBLISHED 상태인 문항 세트와 문항들 조회
     */
    Optional<SurveyQuestionSet> findPublishedByTypeWithQuestions(QuestionSetType type);

    /**
     * 특정 타입의 모든 문항 세트 조회 (버전 관리용)
     */
    List<SurveyQuestionSet> findAllByType(QuestionSetType type);

    /**
     * 특정 타입의 최신 버전 번호 조회
     */
    int findMaxVersionByType(QuestionSetType type);

    void delete(SurveyQuestionSet questionSet);

    /**
     * 특정 타입의 PUBLISHED 상태 세트들을 ARCHIVED로 변경
     */
    void archivePublishedByType(QuestionSetType type);
}
