package com.swcampus.domain.oauth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class OAuthAuthenticationException extends BusinessException {

	public OAuthAuthenticationException() {
		super(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
	}

	public OAuthAuthenticationException(Throwable cause) {
		super(ErrorCode.OAUTH_AUTHENTICATION_FAILED, ErrorCode.OAUTH_AUTHENTICATION_FAILED.getMessage(), cause);
	}
}
