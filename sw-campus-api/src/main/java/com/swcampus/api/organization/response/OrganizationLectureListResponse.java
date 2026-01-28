package com.swcampus.api.organization.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.swcampus.api.lecture.response.LectureSummaryResponse;
import com.swcampus.domain.lecture.dto.LectureSummaryDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기관별 강의 목록 응답")
public record OrganizationLectureListResponse(
        @Schema(description = "강의 목록") List<LectureSummaryResponse> lectures,
        @Schema(description = "페이지 정보") PageInfo page) {

    public static OrganizationLectureListResponse of(List<LectureSummaryResponse> lectures, Page<LectureSummaryDto> page) {
        return new OrganizationLectureListResponse(
                lectures,
                new PageInfo(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages()));
    }

    @Schema(description = "페이지 정보")
    public record PageInfo(
            @Schema(description = "페이지 크기", example = "6") int size,
            @Schema(description = "현재 페이지 번호", example = "0") int number,
            @Schema(description = "전체 요소 수", example = "100") long totalElements,
            @Schema(description = "전체 페이지 수", example = "17") int totalPages) {
    }
}
