package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.Banner;
import com.swcampus.domain.lecture.BannerType;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "BANNERS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "lecture")
public class BannerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banners_seq")
    @SequenceGenerator(name = "banners_seq", sequenceName = "banners_banner_id_seq", allocationSize = 1)
    @Column(name = "BANNER_ID")
    private Long bannerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LECTURE_ID", nullable = false)
    private LectureEntity lecture;

    @Enumerated(EnumType.STRING)
    @Column(name = "BANNER_TYPE")
    private BannerType bannerType;

    @Column(name = "CONTENT", columnDefinition = "TEXT")
    private String content;

    @Column(name = "IMAGE_URL", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "START_DATE", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "END_DATE", nullable = false)
    private OffsetDateTime endDate;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    public static BannerEntity from(Banner banner, LectureEntity lectureEntity) {
        if (banner == null)
            return null;
        return BannerEntity.builder()
                .bannerId(banner.getId())
                .lecture(lectureEntity)
                .bannerType(banner.getType())
                .content(banner.getContent())
                .imageUrl(banner.getImageUrl())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .isActive(banner.getIsActive())
                .build();
    }

    public Banner toDomain() {
        return Banner.builder()
                .id(this.bannerId)
                .lectureId(this.lecture != null ? this.lecture.getLectureId() : null)
                .type(this.bannerType)
                .content(this.content)
                .imageUrl(this.imageUrl)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .isActive(this.isActive)
                .build();
    }

    public void updateFields(Banner banner, LectureEntity lectureEntity) {
        this.lecture = lectureEntity;
        this.bannerType = banner.getType();
        this.content = banner.getContent();
        this.imageUrl = banner.getImageUrl();
        this.startDate = banner.getStartDate();
        this.endDate = banner.getEndDate();
        this.isActive = banner.getIsActive();
    }
}
