package com.swcampus.api.survey.request;

import com.swcampus.domain.survey.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기초 설문 저장 요청")
public class SaveBasicSurveyRequest {

    @Schema(description = "전공 정보")
    @NotNull(message = "전공 정보는 필수입니다")
    @Valid
    private MajorInfoRequest majorInfo;

    @Schema(description = "프로그래밍 경험 유무")
    @NotNull(message = "프로그래밍 경험 정보는 필수입니다")
    @Valid
    private ProgrammingExperienceRequest programmingExperience;

    @Schema(description = "선호 수업 방식", example = "OFFLINE")
    @NotNull(message = "선호 수업 방식은 필수입니다")
    private LearningMethod preferredLearningMethod;

    @Schema(description = "희망 직무 목록", example = "[\"BACKEND\", \"DATA\"]")
    @NotEmpty(message = "희망 직무는 최소 1개 이상 선택해야 합니다")
    private List<DesiredJob> desiredJobs;

    @Schema(description = "희망 직무 기타 입력", example = "보안 엔지니어")
    @Size(max = 100, message = "희망 직무 기타는 100자 이내여야 합니다")
    private String desiredJobOther;

    @Schema(description = "자비 부담 가능 금액 범위", example = "RANGE_100_200")
    @NotNull(message = "자비 부담 가능 금액은 필수입니다")
    private BudgetRange affordableBudgetRange;

    public BasicSurvey toDomain() {
        return BasicSurvey.builder()
                .majorInfo(majorInfo.toDomain())
                .programmingExperience(programmingExperience.toDomain())
                .preferredLearningMethod(preferredLearningMethod)
                .desiredJobs(desiredJobs)
                .desiredJobOther(desiredJobOther)
                .affordableBudgetRange(affordableBudgetRange)
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "전공 정보")
    public static class MajorInfoRequest {
        @Schema(description = "전공 유무", example = "true")
        private boolean hasMajor;

        @Schema(description = "전공명 (전공이 있는 경우)", example = "컴퓨터공학")
        @Size(max = 100, message = "전공명은 100자 이내여야 합니다")
        private String majorName;

        public MajorInfo toDomain() {
            return MajorInfo.builder()
                    .hasMajor(hasMajor)
                    .majorName(majorName)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로그래밍 경험 정보")
    public static class ProgrammingExperienceRequest {
        @Schema(description = "경험 유무", example = "true")
        private boolean hasExperience;

        @Schema(description = "부트캠프/교육과정명 (경험이 있는 경우)", example = "삼성 SW 아카데미")
        @Size(max = 100, message = "부트캠프/교육과정명은 100자 이내여야 합니다")
        private String bootcampName;

        public ProgrammingExperience toDomain() {
            return ProgrammingExperience.builder()
                    .hasExperience(hasExperience)
                    .bootcampName(bootcampName)
                    .build();
        }
    }
}
