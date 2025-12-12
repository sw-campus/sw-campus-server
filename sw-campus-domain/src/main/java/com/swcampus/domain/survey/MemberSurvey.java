package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSurvey {
    private Long userId;
    private String major;
    private Boolean bootcampCompleted;
    private String wantedJobs;
    private String licenses;
    private Boolean hasGovCard;
    private BigDecimal affordableAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberSurvey create(
            Long userId,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        MemberSurvey survey = new MemberSurvey();
        survey.userId = userId;
        survey.major = major;
        survey.bootcampCompleted = bootcampCompleted;
        survey.wantedJobs = wantedJobs;
        survey.licenses = licenses;
        survey.hasGovCard = hasGovCard;
        survey.affordableAmount = affordableAmount;
        return survey;
    }

    public static MemberSurvey of(
            Long userId,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        MemberSurvey survey = new MemberSurvey();
        survey.userId = userId;
        survey.major = major;
        survey.bootcampCompleted = bootcampCompleted;
        survey.wantedJobs = wantedJobs;
        survey.licenses = licenses;
        survey.hasGovCard = hasGovCard;
        survey.affordableAmount = affordableAmount;
        survey.createdAt = createdAt;
        survey.updatedAt = updatedAt;
        return survey;
    }

    public void update(
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        this.major = major;
        this.bootcampCompleted = bootcampCompleted;
        this.wantedJobs = wantedJobs;
        this.licenses = licenses;
        this.hasGovCard = hasGovCard;
        this.affordableAmount = affordableAmount;
    }
}
