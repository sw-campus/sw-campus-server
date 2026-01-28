package com.swcampus.domain.lecture.dto;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureSearchCondition {
    private Long orgId; // 기관 ID (기관별 강의 조회 시 사용)
    private String text; // 제목, 내용, 기관명 검색
    private List<String> regions; // 지역
    private List<Long> categoryIds; // 카테고리 ID 목록 (대/중/소 모두 ID)

    // 비용 필터
    private Boolean isFreeKdt; // 내배카 필요 (무료)
    private Boolean isFreeNoKdt; // 내배카 불필요 (무료)
    private Boolean isPaid; // 유료 (자부담 발생)
    private Integer maxFee; // 자부담 상한액

    // 선발 절차 필터
    private Boolean hasCodingTest;
    private Boolean hasInterview;
    private Boolean hasPreTask;

    // 상태 (RECRUITING, FINISHED)
    private LectureStatus status;
    private LectureAuthStatus lectureAuthStatus;

    private LectureSortType sort; // 정렬 순서

    // 페이지
    private Pageable pageable;

    public Integer getLimit() {
        return pageable != null ? pageable.getPageSize() : null;
    }

    public Long getOffset() {
        return pageable != null ? pageable.getOffset() : null;
    }
}