package com.swcampus.domain.post.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class PostNotFoundException extends BusinessException {

    public PostNotFoundException(Long postId) {
        super(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다. ID: " + postId);
    }

    public PostNotFoundException(String message) {
        super(ErrorCode.POST_NOT_FOUND, message);
    }
}
