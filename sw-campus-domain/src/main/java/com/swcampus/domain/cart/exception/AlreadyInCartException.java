package com.swcampus.domain.cart.exception;

public class AlreadyInCartException extends RuntimeException {
    public AlreadyInCartException(String message) {
        super(message);
    }
}
