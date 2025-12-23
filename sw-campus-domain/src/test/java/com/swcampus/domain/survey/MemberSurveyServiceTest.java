package com.swcampus.domain.survey;

import com.swcampus.domain.survey.exception.SurveyAlreadyExistsException;
import com.swcampus.domain.survey.exception.SurveyNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberSurveyService - 설문조사 서비스 테스트")
class MemberSurveyServiceTest {

    @Mock
    private MemberSurveyRepository surveyRepository;

    @InjectMocks
    private MemberSurveyService surveyService;

    private final Long memberId = 1L;
    private final String major = "컴퓨터공학";
    private final Boolean bootcampCompleted = true;
    private final String wantedJobs = "백엔드 개발자, 데이터 엔지니어";
    private final String licenses = "정보처리기사, SQLD";
    private final Boolean hasGovCard = true;
    private final BigDecimal affordableAmount = BigDecimal.valueOf(500000);

    @Nested
    @DisplayName("설문조사 작성")
    class CreateSurvey {

        @Test
        @DisplayName("성공 - 새 설문조사 생성")
        void success() {
            // given
            when(surveyRepository.existsByMemberId(memberId)).thenReturn(false);
            when(surveyRepository.save(any(MemberSurvey.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            MemberSurvey result = surveyService.createSurvey(
                    memberId, major, bootcampCompleted,
                    wantedJobs, licenses, hasGovCard, affordableAmount
            );

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getMajor()).isEqualTo(major);
            assertThat(result.getBootcampCompleted()).isEqualTo(bootcampCompleted);
            assertThat(result.getWantedJobs()).isEqualTo(wantedJobs);
            assertThat(result.getLicenses()).isEqualTo(licenses);
            assertThat(result.getHasGovCard()).isEqualTo(hasGovCard);
            assertThat(result.getAffordableAmount()).isEqualTo(affordableAmount);

            verify(surveyRepository).existsByMemberId(memberId);
            verify(surveyRepository).save(any(MemberSurvey.class));
        }

        @Test
        @DisplayName("실패 - 이미 설문조사가 존재하면 예외 발생")
        void fail_alreadyExists() {
            // given
            when(surveyRepository.existsByMemberId(memberId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> surveyService.createSurvey(
                    memberId, major, bootcampCompleted,
                    wantedJobs, licenses, hasGovCard, affordableAmount
            ))
                    .isInstanceOf(SurveyAlreadyExistsException.class)
                    .hasMessageContaining("이미 설문조사를 작성하셨습니다");

            verify(surveyRepository).existsByMemberId(memberId);
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
            MemberSurvey survey = MemberSurvey.create(
                    memberId, major, bootcampCompleted,
                    wantedJobs, licenses, hasGovCard, affordableAmount
            );
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(survey));

            // when
            MemberSurvey result = surveyService.getSurveyByMemberId(memberId);

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getMajor()).isEqualTo(major);

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
    @DisplayName("설문조사 수정")
    class UpdateSurvey {

        private final String newMajor = "소프트웨어공학";
        private final Boolean newBootcampCompleted = false;
        private final String newWantedJobs = "풀스택 개발자";
        private final String newLicenses = "정보처리기사, SQLD, AWS SAA";
        private final Boolean newHasGovCard = false;
        private final BigDecimal newAffordableAmount = BigDecimal.valueOf(1000000);

        @Test
        @DisplayName("성공 - 설문조사 수정")
        void success() {
            // given
            MemberSurvey existingSurvey = MemberSurvey.create(
                    memberId, major, bootcampCompleted,
                    wantedJobs, licenses, hasGovCard, affordableAmount
            );
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(existingSurvey));
            when(surveyRepository.save(any(MemberSurvey.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            MemberSurvey result = surveyService.updateSurvey(
                    memberId, newMajor, newBootcampCompleted,
                    newWantedJobs, newLicenses, newHasGovCard, newAffordableAmount
            );

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getMajor()).isEqualTo(newMajor);
            assertThat(result.getBootcampCompleted()).isEqualTo(newBootcampCompleted);
            assertThat(result.getWantedJobs()).isEqualTo(newWantedJobs);
            assertThat(result.getLicenses()).isEqualTo(newLicenses);
            assertThat(result.getHasGovCard()).isEqualTo(newHasGovCard);
            assertThat(result.getAffordableAmount()).isEqualTo(newAffordableAmount);

            verify(surveyRepository).findByMemberId(memberId);
            verify(surveyRepository).save(any(MemberSurvey.class));
        }

        @Test
        @DisplayName("실패 - 설문조사가 없으면 예외 발생")
        void fail_notFound() {
            // given
            when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> surveyService.updateSurvey(
                    memberId, newMajor, newBootcampCompleted,
                    newWantedJobs, newLicenses, newHasGovCard, newAffordableAmount
            ))
                    .isInstanceOf(SurveyNotFoundException.class)
                    .hasMessageContaining("설문조사를 찾을 수 없습니다");

            verify(surveyRepository).findByMemberId(memberId);
            verify(surveyRepository, never()).save(any(MemberSurvey.class));
        }
    }
}
