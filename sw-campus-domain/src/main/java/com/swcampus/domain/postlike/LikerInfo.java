package com.swcampus.domain.postlike;

import lombok.Getter;

/**
 * 게시글 추천자 정보를 담는 도메인 객체
 */
@Getter
public class LikerInfo {
    private final Long id;
    private final String nickname;

    private LikerInfo(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public static LikerInfo of(Long id, String nickname) {
        return new LikerInfo(id, nickname);
    }
}
