package com.swcampus.domain.comment.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CommentNotFoundException extends BusinessException {

    public CommentNotFoundException(Long commentId) {
        super(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다. ID: " + commentId);
    }
}
