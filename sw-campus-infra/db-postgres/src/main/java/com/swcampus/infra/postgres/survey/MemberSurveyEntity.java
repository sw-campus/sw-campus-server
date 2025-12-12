package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "member_surveys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSurveyEntity extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String major;

    @Column(name = "bootcamp_completed")
    private Boolean bootcampCompleted;

    @Column(name = "wanted_jobs", length = 255)
    private String wantedJobs;

    @Column(length = 500)
    private String licenses;

    @Column(name = "has_gov_card")
    private Boolean hasGovCard;

    @Column(name = "affordable_amount", precision = 15, scale = 2)
    private BigDecimal affordableAmount;

    public static MemberSurveyEntity from(MemberSurvey survey) {
        MemberSurveyEntity entity = new MemberSurveyEntity();
        entity.userId = survey.getUserId();
        entity.major = survey.getMajor();
        entity.bootcampCompleted = survey.getBootcampCompleted();
        entity.wantedJobs = survey.getWantedJobs();
        entity.licenses = survey.getLicenses();
        entity.hasGovCard = survey.getHasGovCard();
        entity.affordableAmount = survey.getAffordableAmount();
        return entity;
    }

    public MemberSurvey toDomain() {
        return MemberSurvey.of(
                this.userId,
                this.major,
                this.bootcampCompleted,
                this.wantedJobs,
                this.licenses,
                this.hasGovCard,
                this.affordableAmount,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }

    public void update(MemberSurvey survey) {
        this.major = survey.getMajor();
        this.bootcampCompleted = survey.getBootcampCompleted();
        this.wantedJobs = survey.getWantedJobs();
        this.licenses = survey.getLicenses();
        this.hasGovCard = survey.getHasGovCard();
        this.affordableAmount = survey.getAffordableAmount();
    }
}
