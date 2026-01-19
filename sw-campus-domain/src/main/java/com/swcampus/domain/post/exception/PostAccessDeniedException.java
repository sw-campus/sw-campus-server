package com.swcampus.domain.post.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class PostAccessDeniedException extends BusinessException {

    public PostAccessDeniedException() {
        super(ErrorCode.POST_ACCESS_DENIED);
    }

    public PostAccessDeniedException(String message) {
        super(ErrorCode.POST_ACCESS_DENIED, message);
    }
}
