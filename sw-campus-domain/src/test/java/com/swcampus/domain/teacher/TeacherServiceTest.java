package com.swcampus.domain.teacher;

import com.swcampus.domain.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @InjectMocks
    private TeacherService teacherService;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("강사 생성 성공 - 이미지 포함")
    void createTeacher_WithImage() {
        // given
        Teacher teacher = Teacher.builder()
                .teacherName("Test Teacher")
                .teacherDescription("Test Description")
                .build();
        byte[] imageContent = "image data".getBytes();
        String imageName = "test.jpg";
        String contentType = "image/jpeg";
        String uploadedUrl = "https://s3.example.com/teachers/test.jpg";

        given(fileStorageService.upload(imageContent, "teachers", imageName, contentType))
                .willReturn(uploadedUrl);
        given(teacherRepository.save(any(Teacher.class)))
                .willAnswer(invocation -> {
                    Teacher saved = invocation.getArgument(0);
                    return saved.toBuilder().teacherId(1L).build();
                });

        // when
        Teacher result = teacherService.createTeacher(teacher, imageContent, imageName, contentType);

        // then
        assertThat(result.getTeacherId()).isEqualTo(1L);
        assertThat(result.getTeacherName()).isEqualTo("Test Teacher");
        assertThat(result.getTeacherImageUrl()).isEqualTo(uploadedUrl); // 이미지 URL 확인

        verify(fileStorageService).upload(imageContent, "teachers", imageName, contentType);
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    @DisplayName("강사 생성 성공 - 이미지 없음 (Null)")
    void createTeacher_WithoutImage() {
        // given
        Teacher teacher = Teacher.builder()
                .teacherName("Test Teacher")
                .teacherDescription("Test Description")
                .build();

        given(teacherRepository.save(any(Teacher.class)))
                .willAnswer(invocation -> {
                    Teacher saved = invocation.getArgument(0);
                    return saved.toBuilder().teacherId(1L).build();
                });

        // when
        Teacher result = teacherService.createTeacher(teacher, null, null, null);

        // then
        assertThat(result.getTeacherId()).isEqualTo(1L);
        assertThat(result.getTeacherImageUrl()).isNull(); // 이미지 URL Null 확인

        verify(fileStorageService, never()).upload(any(), any(), any(), any());
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    @DisplayName("강사 생성 성공 - 이미지 비어있음 (Empty Byte Array)")
    void createTeacher_WithEmptyImage() {
        // given
        Teacher teacher = Teacher.builder()
                .teacherName("Test Teacher")
                .teacherDescription("Test Description")
                .build();
        byte[] emptyContent = new byte[0];

        given(teacherRepository.save(any(Teacher.class)))
                .willAnswer(invocation -> {
                    Teacher saved = invocation.getArgument(0);
                    return saved.toBuilder().teacherId(1L).build();
                });

        // when
        Teacher result = teacherService.createTeacher(teacher, emptyContent, "empty.jpg", "image/jpeg");

        // then
        assertThat(result.getTeacherId()).isEqualTo(1L);
        assertThat(result.getTeacherImageUrl()).isNull(); // 이미지 URL Null 확인

        verify(fileStorageService, never()).upload(any(), any(), any(), any());
        verify(teacherRepository).save(any(Teacher.class));
    }
}
