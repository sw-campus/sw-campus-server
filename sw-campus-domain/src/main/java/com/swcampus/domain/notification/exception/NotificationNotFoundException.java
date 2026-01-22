package com.swcampus.domain.notification.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException(Long id) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND,
                String.format("알림을 찾을 수 없습니다. ID: %d", id));
    }
}
