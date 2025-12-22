package com.swcampus.domain.storage.exception;

public class InvalidStorageCategoryException extends RuntimeException {

    public InvalidStorageCategoryException(String category) {
        super("지원하지 않는 카테고리입니다: " + category);
    }
}
