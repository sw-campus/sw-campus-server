package com.swcampus.api.lecture.request;

import java.util.List;

import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSortType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LectureSearchRequest {
    private String text; // 제목, 내용, 기관명 검색
    
    private List<String> regions; // 지역
    
    private List<Long> categoryIds; // 선택된 카테고리 ID 목록

    // 비용
    private Boolean isFreeKdt;
    private Boolean isFreeNoKdt;
    private Boolean isPaid;

    // 선발절차
    private Boolean hasCodingTest;
    private Boolean hasInterview;
    private Boolean hasPreTask;
    
    // 상태 (RECRUITING, ONGOING, FINISHED)
    private String status;
    
    // 정렬 (LATEST, FEE_ASC, START_SOON)
    private LectureSortType sort;

    // 페이지네이션
    private Integer page;
    private Integer size;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MIN_PAGE_NUMBER = 1;

    public LectureSearchCondition toCondition() {
        int pageNum = (this.page == null || this.page < MIN_PAGE_NUMBER) ? MIN_PAGE_NUMBER : this.page;
        int pageSize = (this.size == null || this.size < 1) ? DEFAULT_PAGE_SIZE : this.size;
        long offset = (long) (pageNum - 1) * pageSize;

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
                .status(this.status != null ? com.swcampus.domain.lecture.LectureStatus.valueOf(this.status) : null)
                .sort(this.sort != null ? this.sort : LectureSortType.LATEST)
                .limit(pageSize)
                .offset(offset)
                .build();
    }
}
