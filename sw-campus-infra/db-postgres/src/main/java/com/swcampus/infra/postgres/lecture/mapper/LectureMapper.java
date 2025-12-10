package com.swcampus.infra.postgres.lecture.mapper;

import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.infra.postgres.lecture.LectureEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LectureMapper {
	List<LectureEntity> selectLectures(@Param("cond") LectureSearchCondition condition);
    long countLectures(@Param("cond") LectureSearchCondition condition);
}
