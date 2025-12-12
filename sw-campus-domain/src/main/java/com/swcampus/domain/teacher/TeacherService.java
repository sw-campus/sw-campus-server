package com.swcampus.domain.teacher;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final com.swcampus.domain.storage.FileStorageService fileStorageService;

    public List<Teacher> searchTeachers(String name) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }
        return teacherRepository.searchTeachers(name);
    }

    public Teacher createTeacher(Teacher teacher, byte[] imageContent, String imageName, String contentType) {
        String imageUrl = null;

        if (imageContent != null && imageContent.length > 0) {
            imageUrl = fileStorageService.upload(imageContent, "teachers", imageName, contentType);
        }

        Teacher newTeacher = teacher.toBuilder()
                .teacherImageUrl(imageUrl)
                .build();

        return teacherRepository.save(newTeacher);
    }
}
