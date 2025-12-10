package com.swcampus.infra.postgres.lecture;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swcampus.domain.lecture.LectureStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface LectureJpaRepository extends JpaRepository<LectureEntity, Long> {
    List<LectureEntity> findAllByDeadlineBeforeAndStatus(LocalDateTime now, LectureStatus status);
}