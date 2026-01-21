package com.swcampus.domain.cart.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CartLimitExceededException extends BusinessException {

	public CartLimitExceededException() {
		super(ErrorCode.CART_LIMIT_EXCEEDED);
	}

	public CartLimitExceededException(String message) {
		super(ErrorCode.CART_LIMIT_EXCEEDED, message);
	}
}
