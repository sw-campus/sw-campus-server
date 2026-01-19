package com.swcampus.domain.board.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class BoardCategoryNotFoundException extends BusinessException {

    public BoardCategoryNotFoundException() {
        super(ErrorCode.BOARD_CATEGORY_NOT_FOUND);
    }

    public BoardCategoryNotFoundException(Long id) {
        super(ErrorCode.BOARD_CATEGORY_NOT_FOUND, String.format("카테고리를 찾을 수 없습니다. ID: %d", id));
    }

    public BoardCategoryNotFoundException(String message) {
        super(ErrorCode.BOARD_CATEGORY_NOT_FOUND, message);
    }
}
