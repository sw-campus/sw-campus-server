package com.swcampus.domain.notification.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class NotificationAccessDeniedException extends BusinessException {

    public NotificationAccessDeniedException() {
        super(ErrorCode.NOTIFICATION_ACCESS_DENIED);
    }

    public NotificationAccessDeniedException(String message) {
        super(ErrorCode.NOTIFICATION_ACCESS_DENIED, message);
    }
}
