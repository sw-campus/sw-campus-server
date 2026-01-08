package com.swcampus.domain.cart.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class AlreadyInCartException extends BusinessException {

	public AlreadyInCartException() {
		super(ErrorCode.ALREADY_IN_CART);
	}

	public AlreadyInCartException(String message) {
		super(ErrorCode.ALREADY_IN_CART, message);
	}
}
