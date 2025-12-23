package com.swcampus.domain.storage.exception;

public class StorageKeyNotFoundException extends RuntimeException {

    public StorageKeyNotFoundException(String key) {
        super("존재하지 않는 파일입니다: " + key);
    }
}
