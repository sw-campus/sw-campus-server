package com.swcampus.domain.lecture;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class Banner {
    private Long id;
    private Long lectureId;
    private BannerType type;
    private String url;
    private String imageUrl;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Boolean isActive;
}
