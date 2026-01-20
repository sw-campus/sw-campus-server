package com.swcampus.domain.comment.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CommentAccessDeniedException extends BusinessException {

    public CommentAccessDeniedException(String message) {
        super(ErrorCode.COMMENT_ACCESS_DENIED, message);
    }
}
