package com.swcampus.domain.lecture;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 강의 캐시 저장소 인터페이스
 * Redis 등 캐시 저장소에서 강의 정보를 조회/저장/삭제
 */
public interface LectureCacheRepository {

    /**
     * 캐시에서 강의 조회
     * 
     * @param lectureId 강의 ID
     * @return 캐시된 강의 (없으면 empty)
     */
    Optional<Lecture> getLecture(Long lectureId);

    /**
     * 캐시에 강의 저장
     * 
     * @param lecture 저장할 강의
     */
    void saveLecture(Lecture lecture);

    /**
     * 캐시에서 강의 삭제
     * 
     * @param lectureId 삭제할 강의 ID
     */
    void deleteLecture(Long lectureId);

    /**
     * 캐시에서 여러 강의 조회
     * 
     * @param lectureIds 강의 ID 목록
     * @return 캐시된 강의 Map (ID -> Lecture)
     */
    Map<Long, Lecture> getLectures(List<Long> lectureIds);

    /**
     * 캐시에 여러 강의 저장
     * 
     * @param lectures 저장할 강의 목록
     */
    void saveLectures(List<Lecture> lectures);
}
