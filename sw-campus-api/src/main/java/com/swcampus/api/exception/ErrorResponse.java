package com.swcampus.api.exception;

import java.time.LocalDateTime;

import com.swcampus.shared.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "에러 응답")
public class ErrorResponse {

	@Schema(description = "에러 코드", example = "M001")
	private String code;

	@Schema(description = "HTTP 상태 코드", example = "400")
	private int status;

	@Schema(description = "에러 메시지", example = "잘못된 요청입니다")
	private String message;

	@Schema(description = "발생 시각", example = "2025-12-09T12:00:00")
	private LocalDateTime timestamp;

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(
				errorCode.getCode(),
				errorCode.getStatus(),
				errorCode.getMessage(),
				LocalDateTime.now());
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(
				errorCode.getCode(),
				errorCode.getStatus(),
				message,
				LocalDateTime.now());
	}
}
