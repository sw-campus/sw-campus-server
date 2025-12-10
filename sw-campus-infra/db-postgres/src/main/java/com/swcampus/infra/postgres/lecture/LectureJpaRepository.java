package com.swcampus.infra.postgres.lecture;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.swcampus.domain.lecture.LectureStatus;

public interface LectureJpaRepository extends JpaRepository<LectureEntity, Long> {
    List<LectureEntity> findAllByDeadlineBeforeAndStatus(LocalDateTime now, LectureStatus status);
    
    List<LectureEntity> findAllByOrgId(Long orgId);

    @Query("SELECT l.orgId, COUNT(l) FROM LectureEntity l WHERE l.status = :status GROUP BY l.orgId")
    List<Object[]> countByStatusGroupByOrgId(@Param("status") LectureStatus status);
}