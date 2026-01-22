package com.swcampus.domain.post;

/**
 * 게시글 조회 기록 저장소 인터페이스
 * 중복 조회수 증가를 방지하기 위해 사용
 */
public interface PostViewRepository {

    /**
     * 이미 조회한 게시글인지 확인하고, 아니라면 조회 기록을 저장
     *
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @param ttlSeconds 조회 기록 유지 시간 (초 단위)
     * @return 이미 조회한 경우 true, 처음 조회인 경우 false
     */
    boolean hasViewed(Long postId, Long userId, long ttlSeconds);
}
