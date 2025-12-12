package com.swcampus.api.teacher;

import java.util.List;

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

@Tag(name = "Teacher", description = "강사 관리 API")
@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "강사 검색", description = "강사 이름으로 검색합니다.")
    @GetMapping
    public List<TeacherResponse> searchTeachers(
            @RequestParam(required = false, defaultValue = "") String name) {
        return teacherService.searchTeachers(name).stream()
                .map(TeacherResponse::from)
                .toList();
    }

    @Operation(summary = "강사 등록", description = "새로운 강사를 등록합니다. (이미지 포함)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherResponse> createTeacher(
            @Parameter(description = "강사 정보 (JSON)") @RequestPart("teacher") TeacherCreateRequest request,
            @Parameter(description = "강사 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image)
            throws java.io.IOException {

        byte[] imageContent = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        String imageName = (image != null && !image.isEmpty()) ? image.getOriginalFilename() : null;
        String contentType = (image != null && !image.isEmpty()) ? image.getContentType() : null;

        Teacher savedTeacher = teacherService.createTeacher(request.toDomain(),
                imageContent, imageName, contentType);

        return ResponseEntity.ok(TeacherResponse.from(savedTeacher));
    }
}
