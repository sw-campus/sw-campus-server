package com.swcampus.api.teacher;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.swcampus.api.teacher.request.TeacherCreateRequest;
import com.swcampus.api.teacher.response.TeacherResponse;
import com.swcampus.domain.teacher.Teacher;
import com.swcampus.domain.teacher.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Teacher", description = "강사 관리 API")
@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "강사 검색", description = "강사 이름으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public List<TeacherResponse> searchTeachers(
            @Parameter(description = "강사 이름", example = "김철수")
            @RequestParam(required = false, defaultValue = "") String name) {
        return teacherService.searchTeachers(name).stream()
                .map(TeacherResponse::from)
                .toList();
    }

    @Operation(summary = "강사 등록", description = "새로운 강사를 등록합니다. (이미지 포함)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherResponse> createTeacher(
            @Parameter(description = "강사 정보 (JSON)") @Valid @RequestPart("teacher") TeacherCreateRequest request,
            @Parameter(description = "강사 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image)
            throws java.io.IOException {

        byte[] imageContent = null;
        String imageName = null;
        String contentType = null;
        if (image != null && !image.isEmpty()) {
            imageContent = image.getBytes();
            imageName = image.getOriginalFilename();
            contentType = image.getContentType();
        }

        Teacher savedTeacher = teacherService.createTeacher(request.toDomain(),
                imageContent, imageName, contentType);

        return ResponseEntity.status(HttpStatus.CREATED).body(TeacherResponse.from(savedTeacher));
    }
}
