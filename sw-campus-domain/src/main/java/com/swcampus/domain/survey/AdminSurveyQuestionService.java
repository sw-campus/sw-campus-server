package com.swcampus.domain.survey;

import com.swcampus.domain.survey.exception.QuestionSetNotEditableException;
import com.swcampus.domain.survey.exception.SurveyOptionNotFoundException;
import com.swcampus.domain.survey.exception.SurveyQuestionNotFoundException;
import com.swcampus.domain.survey.exception.SurveyQuestionSetNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSurveyQuestionService {

    private final SurveyQuestionSetRepository questionSetRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyOptionRepository optionRepository;

    /**
     * 새 문항 세트 생성 (DRAFT 상태, APTITUDE 타입 고정)
     * 버전은 최대 버전 + 1로 자동 설정
     * BASIC 타입은 코드로 관리되므로 문항 세트 생성 대상이 아님
     */
    @Transactional
    public SurveyQuestionSet createQuestionSet(String name, String description) {
        QuestionSetType type = QuestionSetType.APTITUDE;
        int nextVersion = questionSetRepository.findMaxVersionByType(type) + 1;
        SurveyQuestionSet questionSet = SurveyQuestionSet.createDraft(name, description, type, nextVersion);
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
     * 문항 세트의 Part별 문항 수 조회
     */
    public Map<QuestionPart, Integer> getQuestionCountsByPart(Long questionSetId) {
        SurveyQuestionSet questionSet = getQuestionSetWithQuestions(questionSetId);
        return questionSet.getQuestions().stream()
                .filter(q -> q.getPart() != null)
                .collect(Collectors.groupingBy(
                        SurveyQuestion::getPart,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
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
     * 문항 세트 삭제 (DRAFT, ARCHIVED 상태만 가능)
     */
    @Transactional
    public void deleteQuestionSet(Long questionSetId) {
        SurveyQuestionSet questionSet = getQuestionSet(questionSetId);
        if (!questionSet.isDeletable()) {
            throw new IllegalStateException("발행된 문항 세트는 삭제할 수 없습니다. (ID: " + questionSetId + ")");
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
     * ARCHIVED 상태의 문항 세트를 다시 발행 (롤백)
     * - 기존 PUBLISHED 세트는 ARCHIVED로 변경
     * - 현재 ARCHIVED 세트를 PUBLISHED로 변경
     */
    @Transactional
    public SurveyQuestionSet republishQuestionSet(Long questionSetId) {
        SurveyQuestionSet questionSet = getQuestionSet(questionSetId);

        if (questionSet.getStatus() != QuestionSetStatus.ARCHIVED) {
            throw new IllegalStateException("ARCHIVED 상태의 문항 세트만 재발행할 수 있습니다.");
        }

        // 기존 PUBLISHED → ARCHIVED
        questionSetRepository.archivePublishedByType(questionSet.getType());

        // 현재 세트 재발행
        questionSet.republish();
        return questionSetRepository.save(questionSet);
    }

    /**
     * 기존 세트를 복제하여 새 버전 생성 (문항 및 선택지 포함)
     */
    @Transactional
    public SurveyQuestionSet cloneQuestionSet(Long questionSetId) {
        SurveyQuestionSet original = getQuestionSetWithQuestions(questionSetId);

        // 1. QuestionSet 복제 및 저장
        int newVersion = questionSetRepository.findMaxVersionByType(original.getType()) + 1;
        SurveyQuestionSet clonedSet = original.cloneForNewVersion(newVersion);
        SurveyQuestionSet savedSet = questionSetRepository.save(clonedSet);

        // 2. Question 복제
        for (SurveyQuestion originalQuestion : original.getQuestions()) {
            SurveyQuestion clonedQuestion = SurveyQuestion.builder()
                    .questionSetId(savedSet.getQuestionSetId())
                    .questionOrder(originalQuestion.getQuestionOrder())
                    .questionText(originalQuestion.getQuestionText())
                    .questionType(originalQuestion.getQuestionType())
                    .isRequired(originalQuestion.isRequired())
                    .fieldKey(originalQuestion.getFieldKey())
                    .part(originalQuestion.getPart())
                    .showCondition(originalQuestion.getShowCondition())
                    .metadata(originalQuestion.getMetadata())
                    .build();

            SurveyQuestion savedQuestion = questionRepository.save(clonedQuestion);

            // 3. Option 복제
            for (SurveyOption originalOption : originalQuestion.getOptions()) {
                SurveyOption clonedOption = SurveyOption.builder()
                        .questionId(savedQuestion.getQuestionId())
                        .optionOrder(originalOption.getOptionOrder())
                        .optionText(originalOption.getOptionText())
                        .optionValue(originalOption.getOptionValue())
                        .score(originalOption.getScore())
                        .jobType(originalOption.getJobType())
                        .isCorrect(originalOption.getIsCorrect())
                        .build();

                optionRepository.save(clonedOption);
            }
        }

        return savedSet;
    }

    /**
     * PUBLISHED 상태의 문항 세트 조회 (Admin API용 - 없으면 예외)
     */
    public SurveyQuestionSet getPublishedQuestionSet(QuestionSetType type) {
        return questionSetRepository.findPublishedByTypeWithQuestions(type)
                .orElseThrow(() -> new SurveyQuestionSetNotFoundException(type.name()));
    }

    /**
     * PUBLISHED 상태의 문항 세트 조회 (사용자 API용 - Optional 반환)
     */
    public Optional<SurveyQuestionSet> findPublishedQuestionSet(QuestionSetType type) {
        return questionSetRepository.findPublishedByTypeWithQuestions(type);
    }

    /**
     * 전체 문항 세트 조회
     */
    public List<SurveyQuestionSet> getAllQuestionSets() {
        return questionSetRepository.findAll();
    }

    // ===== Question CRUD =====

    /**
     * 문항 추가
     * fieldKey는 "q{순서}" 형식으로 자동 생성됩니다.
     */
    @Transactional
    public SurveyQuestion addQuestion(
            Long questionSetId,
            String questionText,
            QuestionType questionType,
            boolean isRequired,
            QuestionPart part,
            Map<String, Object> showCondition,
            Map<String, Object> metadata
    ) {
        SurveyQuestionSet questionSet = getQuestionSet(questionSetId);
        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(questionSetId);
        }

        int nextOrder = questionRepository.findMaxOrderByQuestionSetId(questionSetId) + 1;
        String fieldKey = "q" + nextOrder;

        SurveyQuestion question = SurveyQuestion.builder()
                .questionSetId(questionSetId)
                .questionOrder(nextOrder)
                .questionText(questionText)
                .questionType(questionType)
                .isRequired(isRequired)
                .fieldKey(fieldKey)
                .part(part)
                .showCondition(showCondition)
                .metadata(metadata)
                .build();

        return questionRepository.save(question);
    }

    /**
     * 문항 조회
     */
    public SurveyQuestion getQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new SurveyQuestionNotFoundException(questionId));
    }

    /**
     * 문항 상세 조회 (선택지 포함)
     */
    public SurveyQuestion getQuestionWithOptions(Long questionId) {
        return questionRepository.findByIdWithOptions(questionId)
                .orElseThrow(() -> new SurveyQuestionNotFoundException(questionId));
    }

    /**
     * 문항 수정
     */
    @Transactional
    public SurveyQuestion updateQuestion(
            Long questionId,
            String questionText,
            QuestionType questionType,
            boolean isRequired,
            String fieldKey,
            QuestionPart part,
            Map<String, Object> showCondition,
            Map<String, Object> metadata
    ) {
        SurveyQuestion question = getQuestion(questionId);
        SurveyQuestionSet questionSet = getQuestionSet(question.getQuestionSetId());

        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(question.getQuestionSetId());
        }

        question.update(questionText, questionType, isRequired, fieldKey, showCondition, metadata, part);
        return questionRepository.save(question);
    }

    /**
     * 문항 삭제
     * 삭제 후 남은 문항들의 순서를 1부터 재정렬합니다.
     */
    @Transactional
    public void deleteQuestion(Long questionId) {
        SurveyQuestion question = getQuestion(questionId);
        SurveyQuestionSet questionSet = getQuestionSet(question.getQuestionSetId());

        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(question.getQuestionSetId());
        }

        Long questionSetId = question.getQuestionSetId();
        questionRepository.delete(question);

        // 남은 문항들의 순서를 1부터 재정렬
        reorderQuestions(questionSetId);
    }

    /**
     * 문항 세트 내 문항들의 순서를 1부터 재정렬합니다.
     */
    private void reorderQuestions(Long questionSetId) {
        List<SurveyQuestion> questions = questionRepository.findAllByQuestionSetIdOrderByQuestionOrder(questionSetId);
        int order = 1;
        for (SurveyQuestion q : questions) {
            if (q.getQuestionOrder() != order) {
                q.updateOrder(order);
                questionRepository.save(q);
            }
            order++;
        }
    }

    // ===== Option CRUD =====

    /**
     * 선택지 추가
     * optionValue는 Part에 따라 자동 생성됩니다:
     * - PART3: jobType 값 (F, B, D)
     * - PART1, PART2: optionOrder 값 (1, 2, 3...)
     */
    @Transactional
    public SurveyOption addOption(
            Long questionId,
            String optionText,
            Integer score,
            JobTypeCode jobType,
            Boolean isCorrect
    ) {
        SurveyQuestion question = getQuestion(questionId);
        SurveyQuestionSet questionSet = getQuestionSet(question.getQuestionSetId());

        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(question.getQuestionSetId());
        }

        int nextOrder = optionRepository.findMaxOrderByQuestionId(questionId) + 1;

        // optionValue 자동 생성: PART3는 jobType, 그 외는 순서 번호
        String optionValue = (question.getPart() == QuestionPart.PART3 && jobType != null)
                ? jobType.name()
                : String.valueOf(nextOrder);

        SurveyOption option = SurveyOption.builder()
                .questionId(questionId)
                .optionOrder(nextOrder)
                .optionText(optionText)
                .optionValue(optionValue)
                .score(score)
                .jobType(jobType)
                .isCorrect(isCorrect)
                .build();

        return optionRepository.save(option);
    }

    /**
     * 선택지 조회
     */
    public SurveyOption getOption(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new SurveyOptionNotFoundException(optionId));
    }

    /**
     * 선택지 수정
     * optionValue는 Part에 따라 자동 갱신됩니다:
     * - PART3: jobType 변경 시 해당 값으로 갱신
     * - PART1, PART2: 기존 optionOrder 유지
     */
    @Transactional
    public SurveyOption updateOption(
            Long optionId,
            String optionText,
            Integer score,
            JobTypeCode jobType,
            Boolean isCorrect
    ) {
        SurveyOption option = getOption(optionId);
        SurveyQuestion question = getQuestion(option.getQuestionId());
        SurveyQuestionSet questionSet = getQuestionSet(question.getQuestionSetId());

        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(question.getQuestionSetId());
        }

        // optionValue 자동 갱신: PART3는 jobType, 그 외는 기존 순서 유지
        String optionValue = (question.getPart() == QuestionPart.PART3 && jobType != null)
                ? jobType.name()
                : String.valueOf(option.getOptionOrder());

        option.update(optionText, optionValue, score, jobType, isCorrect);
        return optionRepository.save(option);
    }

    /**
     * 선택지 삭제
     * 삭제 후 남은 선택지들의 순서를 1부터 재정렬합니다.
     */
    @Transactional
    public void deleteOption(Long optionId) {
        SurveyOption option = getOption(optionId);
        SurveyQuestion question = getQuestion(option.getQuestionId());
        SurveyQuestionSet questionSet = getQuestionSet(question.getQuestionSetId());

        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(question.getQuestionSetId());
        }

        Long questionId = option.getQuestionId();
        optionRepository.delete(option);

        // 남은 선택지들의 순서를 1부터 재정렬
        reorderOptions(questionId);
    }

    /**
     * 문항 내 선택지들의 순서를 1부터 재정렬합니다.
     */
    private void reorderOptions(Long questionId) {
        List<SurveyOption> options = optionRepository.findAllByQuestionIdOrderByOptionOrder(questionId);
        int order = 1;
        for (SurveyOption o : options) {
            if (o.getOptionOrder() != order) {
                o.updateOrder(order);
                optionRepository.save(o);
            }
            order++;
        }
    }

    /**
     * 문항 순서 변경
     * 드래그 앤 드롭으로 문항의 순서를 변경합니다.
     */
    @Transactional
    public void reorderQuestion(Long questionId, int newOrder) {
        SurveyQuestion question = getQuestion(questionId);
        SurveyQuestionSet questionSet = getQuestionSet(question.getQuestionSetId());

        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(question.getQuestionSetId());
        }

        int oldOrder = question.getQuestionOrder();
        if (oldOrder == newOrder) {
            return;
        }

        List<SurveyQuestion> questions = questionRepository.findAllByQuestionSetIdOrderByQuestionOrder(question.getQuestionSetId());

        if (oldOrder < newOrder) {
            // 아래로 이동: oldOrder+1 ~ newOrder 범위의 문항들을 1씩 위로 이동
            for (SurveyQuestion q : questions) {
                int order = q.getQuestionOrder();
                if (order > oldOrder && order <= newOrder) {
                    q.updateOrder(order - 1);
                    questionRepository.save(q);
                }
            }
        } else {
            // 위로 이동: newOrder ~ oldOrder-1 범위의 문항들을 1씩 아래로 이동
            for (SurveyQuestion q : questions) {
                int order = q.getQuestionOrder();
                if (order >= newOrder && order < oldOrder) {
                    q.updateOrder(order + 1);
                    questionRepository.save(q);
                }
            }
        }

        question.updateOrder(newOrder);
        questionRepository.save(question);
    }

    /**
     * 선택지 순서 변경
     * 드래그 앤 드롭으로 선택지의 순서를 변경합니다.
     */
    @Transactional
    public void reorderOption(Long optionId, int newOrder) {
        SurveyOption option = getOption(optionId);
        SurveyQuestion question = getQuestion(option.getQuestionId());
        SurveyQuestionSet questionSet = getQuestionSet(question.getQuestionSetId());

        if (!questionSet.isEditable()) {
            throw new QuestionSetNotEditableException(question.getQuestionSetId());
        }

        int oldOrder = option.getOptionOrder();
        if (oldOrder == newOrder) {
            return;
        }

        List<SurveyOption> options = optionRepository.findAllByQuestionIdOrderByOptionOrder(option.getQuestionId());

        if (oldOrder < newOrder) {
            // 아래로 이동: oldOrder+1 ~ newOrder 범위의 선택지들을 1씩 위로 이동
            for (SurveyOption o : options) {
                int order = o.getOptionOrder();
                if (order > oldOrder && order <= newOrder) {
                    o.updateOrder(order - 1);
                    optionRepository.save(o);
                }
            }
        } else {
            // 위로 이동: newOrder ~ oldOrder-1 범위의 선택지들을 1씩 아래로 이동
            for (SurveyOption o : options) {
                int order = o.getOptionOrder();
                if (order >= newOrder && order < oldOrder) {
                    o.updateOrder(order + 1);
                    optionRepository.save(o);
                }
            }
        }

        option.updateOrder(newOrder);
        optionRepository.save(option);
    }
}
