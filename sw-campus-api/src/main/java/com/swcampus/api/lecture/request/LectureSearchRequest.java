package com.swcampus.api.lecture.request;

import java.util.List;

import org.springframework.data.domain.PageRequest;

import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSortType;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LectureSearchRequest {

    @Parameter(description = "검색 키워드 (강의명, 기관명)", example = "웹 개발")
    private String text;

    @Parameter(description = "지역 목록", hidden = true)
    private List<String> regions;

    @Parameter(description = "카테고리 ID 목록", hidden = true)
    private List<Long> categoryIds;

    @Parameter(description = "국비지원 무료", hidden = true)
    private Boolean isFreeKdt;

    @Parameter(description = "비국비 무료", example = "false")
    private Boolean isFreeNoKdt;

    @Parameter(description = "유료", example = "false")
    private Boolean isPaid;

    @Parameter(description = "코딩테스트 있음", hidden = true)
    private Boolean hasCodingTest;

    @Parameter(description = "면접 있음", example = "true")
    private Boolean hasInterview;

    @Parameter(description = "사전과제 있음", example = "false")
    private Boolean hasPreTask;

    @Parameter(description = "모집 상태 (RECRUITING: 모집중, FINISHED: 마감)", example = "RECRUITING")
    private String status;

    @Parameter(description = "정렬 기준 (LATEST: 최신순, FEE_ASC: 비용낮은순, START_SOON: 시작임박순)", example = "LATEST")
    private LectureSortType sort;

    @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
    private Integer page;

    @Parameter(description = "페이지 크기", example = "20")
    private Integer size;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MIN_PAGE_NUMBER = 1;

    public LectureSearchCondition toCondition() {
        int pageNum = (this.page == null || this.page < MIN_PAGE_NUMBER) ? MIN_PAGE_NUMBER : this.page;
        int pageSize = (this.size == null || this.size < 1) ? DEFAULT_PAGE_SIZE : this.size;

        return LectureSearchCondition.builder()
                .text((this.text != null && !this.text.trim().isEmpty()) ? this.text : null)
                .regions((this.regions != null && !this.regions.isEmpty()) ? this.regions : null)
                .categoryIds((this.categoryIds != null && !this.categoryIds.isEmpty()) ? this.categoryIds : null)
                .isFreeKdt(this.isFreeKdt)
                .isFreeNoKdt(this.isFreeNoKdt)
                .isPaid(this.isPaid)
                .hasCodingTest(this.hasCodingTest)
                .hasInterview(this.hasInterview)
                .hasPreTask(this.hasPreTask)
                .status(this.status != null ? LectureStatus.valueOf(this.status) : null)
                .lectureAuthStatus(LectureAuthStatus.APPROVED)
                .sort(this.sort != null ? this.sort : LectureSortType.LATEST)
                .pageable(PageRequest.of(pageNum - 1, pageSize))
                .build();
    }
}
