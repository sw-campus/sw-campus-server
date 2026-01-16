package com.swcampus.domain.survey;

import com.swcampus.domain.survey.exception.AptitudeTestRequiredException;
import com.swcampus.domain.survey.exception.BasicSurveyRequiredException;
import com.swcampus.domain.survey.exception.SurveyNotFoundException;
import com.swcampus.domain.survey.exception.SurveyQuestionSetNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberSurveyService - 설문조사 서비스 테스트")
class MemberSurveyServiceTest {

    @Mock
    private MemberSurveyRepository surveyRepository;

    @Mock
    private SurveyQuestionSetRepository questionSetRepository;

    @Mock
    private SurveyResultCalculator resultCalculator;

    @InjectMocks
    private MemberSurveyService surveyService;

    private final Long memberId = 1L;

    private BasicSurvey createTestBasicSurvey() {
        return BasicSurvey.builder()
                .majorInfo(MajorInfo.withMajor("컴퓨터공학"))
                .programmingExperience(ProgrammingExperience.withExperience("삼성 SW 아카데미"))
                .preferredLearningMethod(LearningMethod.OFFLINE)
                .desiredJobs(List.of(DesiredJob.BACKEND, DesiredJob.DATA))
                .desiredJobOther(null)
                .affordableBudgetRange(BudgetRange.RANGE_100_200)
                .build();
    }

    private AptitudeTest createTestAptitudeTest() {
        return AptitudeTest.builder()
                .part1Answers(Map.of("q1", 2, "q2", 1, "q3", 2, "q4", 3))
                .part2Answers(Map.of("q5", 3, "q6", 2, "q7", 2, "q8", 3))
                .part3Answers(Map.of(
                        "q9", "B", "q10", "B", "q11", "D",
                        "q12", "B", "q13", "B", "q14", "B", "q15", "B"
                ))
                .build();
    }

    private SurveyQuestionSet createTestQuestionSet() {
        return SurveyQuestionSet.builder()
                .questionSetId(1L)
                .name("성향 테스트 v1")
                .type(QuestionSetType.APTITUDE)
                .version(1)
                .status(QuestionSetStatus.PUBLISHED)
                .questions(List.of())
                .build();
    }

    private SurveyResults createTestResults() {
        return SurveyResults.builder()
                .aptitudeScore(65)
                .aptitudeGrade(AptitudeGrade.TALENTED)
                .jobTypeScores(Map.of(JobTypeCode.B, 5, JobTypeCode.F, 1, JobTypeCode.D, 1))
                .recommendedJob(RecommendedJob.BACKEND)
                .build();
    }

    @Nested
    @DisplayName("기초 설문 저장")
    class SaveBasicSurvey {

        @Test
        @DisplayName("성공 - 새 기초 설문 생성")
        void success_createNew() {
            // given
            BasicSurvey basicSurvey = createTestBasicSurvey();
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
            when(surveyRepository.save(any(MemberSurvey.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            MemberSurvey result = surveyService.saveBasicSurvey(memberId, basicSurvey);

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getBasicSurvey()).isNotNull();
            assertThat(result.getBasicSurvey().getMajorInfo().getMajorName()).isEqualTo("컴퓨터공학");
            assertThat(result.hasBasicSurvey()).isTrue();
            assertThat(result.hasAptitudeTest()).isFalse();

            verify(surveyRepository).findByMemberId(memberId);
            verify(surveyRepository).save(any(MemberSurvey.class));
        }

        @Test
        @DisplayName("성공 - 기존 설문 업데이트")
        void success_update() {
            // given
            BasicSurvey oldBasicSurvey = BasicSurvey.builder()
                    .majorInfo(MajorInfo.withMajor("전자공학"))
                    .programmingExperience(ProgrammingExperience.noExperience())
                    .preferredLearningMethod(LearningMethod.ONLINE)
                    .desiredJobs(List.of(DesiredJob.FRONTEND))
                    .affordableBudgetRange(BudgetRange.UNDER_50)
                    .build();
            MemberSurvey existingSurvey = MemberSurvey.createWithBasicSurvey(memberId, oldBasicSurvey);

            BasicSurvey newBasicSurvey = createTestBasicSurvey();

            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(existingSurvey));
            when(surveyRepository.save(any(MemberSurvey.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            MemberSurvey result = surveyService.saveBasicSurvey(memberId, newBasicSurvey);

            // then
            assertThat(result.getBasicSurvey().getMajorInfo().getMajorName()).isEqualTo("컴퓨터공학");
            assertThat(result.getBasicSurvey().getPreferredLearningMethod()).isEqualTo(LearningMethod.OFFLINE);

            verify(surveyRepository).findByMemberId(memberId);
            verify(surveyRepository).save(any(MemberSurvey.class));
        }
    }

    @Nested
    @DisplayName("성향 테스트 제출")
    class SubmitAptitudeTest {

        @Test
        @DisplayName("성공 - 성향 테스트 제출 및 결과 계산")
        void success() {
            // given
            BasicSurvey basicSurvey = createTestBasicSurvey();
            MemberSurvey existingSurvey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);
            AptitudeTest aptitudeTest = createTestAptitudeTest();
            SurveyQuestionSet questionSet = createTestQuestionSet();
            SurveyResults expectedResults = createTestResults();

            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(existingSurvey));
            when(questionSetRepository.findPublishedByTypeWithQuestions(QuestionSetType.APTITUDE))
                    .thenReturn(Optional.of(questionSet));
            when(resultCalculator.calculate(eq(aptitudeTest), eq(questionSet)))
                    .thenReturn(expectedResults);
            when(surveyRepository.save(any(MemberSurvey.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            MemberSurvey result = surveyService.submitAptitudeTest(memberId, aptitudeTest);

            // then
            assertThat(result.hasAptitudeTest()).isTrue();
            assertThat(result.getResults()).isNotNull();
            assertThat(result.getResults().getRecommendedJob()).isEqualTo(RecommendedJob.BACKEND);
            assertThat(result.getCompletedAt()).isNotNull();

            verify(surveyRepository).findByMemberId(memberId);
            verify(questionSetRepository).findPublishedByTypeWithQuestions(QuestionSetType.APTITUDE);
            verify(resultCalculator).calculate(aptitudeTest, questionSet);
            verify(surveyRepository).save(any(MemberSurvey.class));
        }

        @Test
        @DisplayName("실패 - 설문조사가 없으면 예외 발생")
        void fail_surveyNotFound() {
            // given
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            AptitudeTest aptitudeTest = createTestAptitudeTest();

            // when & then
            assertThatThrownBy(() -> surveyService.submitAptitudeTest(memberId, aptitudeTest))
                    .isInstanceOf(SurveyNotFoundException.class);

            verify(surveyRepository).findByMemberId(memberId);
            verify(surveyRepository, never()).save(any(MemberSurvey.class));
        }

        @Test
        @DisplayName("실패 - 기초 설문이 비어있는 상태에서 성향 테스트 제출")
        void fail_emptyBasicSurvey() {
            // given
            MemberSurvey existingSurvey = MemberSurvey.createWithBasicSurvey(memberId, null);
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(existingSurvey));

            AptitudeTest aptitudeTest = createTestAptitudeTest();

            // when & then
            assertThatThrownBy(() -> surveyService.submitAptitudeTest(memberId, aptitudeTest))
                    .isInstanceOf(BasicSurveyRequiredException.class);

            verify(surveyRepository).findByMemberId(memberId);
            verify(surveyRepository, never()).save(any(MemberSurvey.class));
        }

        @Test
        @DisplayName("실패 - 발행된 문항 세트가 없는 경우")
        void fail_noQuestionSet() {
            // given
            BasicSurvey basicSurvey = createTestBasicSurvey();
            MemberSurvey existingSurvey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);
            AptitudeTest aptitudeTest = createTestAptitudeTest();

            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(existingSurvey));
            when(questionSetRepository.findPublishedByTypeWithQuestions(QuestionSetType.APTITUDE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> surveyService.submitAptitudeTest(memberId, aptitudeTest))
                    .isInstanceOf(SurveyQuestionSetNotFoundException.class);

            verify(surveyRepository).findByMemberId(memberId);
            verify(surveyRepository, never()).save(any(MemberSurvey.class));
        }
    }

    @Nested
    @DisplayName("설문조사 조회")
    class GetSurveyByMemberId {

        @Test
        @DisplayName("성공 - 설문조사 조회")
        void success() {
            // given
            BasicSurvey basicSurvey = createTestBasicSurvey();
            MemberSurvey survey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(survey));

            // when
            MemberSurvey result = surveyService.getSurveyByMemberId(memberId);

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getBasicSurvey().getMajorInfo().getMajorName()).isEqualTo("컴퓨터공학");

            verify(surveyRepository).findByMemberId(memberId);
        }

        @Test
        @DisplayName("실패 - 설문조사가 없으면 예외 발생")
        void fail_notFound() {
            // given
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> surveyService.getSurveyByMemberId(memberId))
                    .isInstanceOf(SurveyNotFoundException.class)
                    .hasMessageContaining("설문조사를 찾을 수 없습니다");

            verify(surveyRepository).findByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("설문 결과 조회")
    class GetResultsByMemberId {

        @Test
        @DisplayName("성공 - 설문 결과 조회")
        void success() {
            // given
            BasicSurvey basicSurvey = createTestBasicSurvey();
            AptitudeTest aptitudeTest = createTestAptitudeTest();
            SurveyResults expectedResults = createTestResults();
            MemberSurvey survey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);
            survey.completeAptitudeTest(aptitudeTest, expectedResults, 1, java.time.LocalDateTime.now());

            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(survey));

            // when
            SurveyResults result = surveyService.getResultsByMemberId(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRecommendedJob()).isEqualTo(RecommendedJob.BACKEND);

            verify(surveyRepository).findByMemberId(memberId);
        }

        @Test
        @DisplayName("실패 - 성향 테스트 미완료")
        void fail_noAptitudeTest() {
            // given
            BasicSurvey basicSurvey = createTestBasicSurvey();
            MemberSurvey survey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);

            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(survey));

            // when & then
            assertThatThrownBy(() -> surveyService.getResultsByMemberId(memberId))
                    .isInstanceOf(AptitudeTestRequiredException.class);

            verify(surveyRepository).findByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("설문 존재 여부 확인")
    class ExistsByMemberId {

        @Test
        @DisplayName("설문 존재함")
        void exists() {
            // given
            when(surveyRepository.existsByMemberId(memberId)).thenReturn(true);

            // when
            boolean result = surveyService.existsByMemberId(memberId);

            // then
            assertThat(result).isTrue();
            verify(surveyRepository).existsByMemberId(memberId);
        }

        @Test
        @DisplayName("설문 존재하지 않음")
        void notExists() {
            // given
            when(surveyRepository.existsByMemberId(memberId)).thenReturn(false);

            // when
            boolean result = surveyService.existsByMemberId(memberId);

            // then
            assertThat(result).isFalse();
            verify(surveyRepository).existsByMemberId(memberId);
        }
    }
}
