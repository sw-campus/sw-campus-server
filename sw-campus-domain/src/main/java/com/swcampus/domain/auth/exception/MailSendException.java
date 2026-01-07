package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class MailSendException extends BusinessException {

	public MailSendException(String message) {
		super(ErrorCode.MAIL_SEND_FAILED, message);
	}

	public MailSendException(String message, Throwable cause) {
		super(ErrorCode.MAIL_SEND_FAILED, message, cause);
	}
}
