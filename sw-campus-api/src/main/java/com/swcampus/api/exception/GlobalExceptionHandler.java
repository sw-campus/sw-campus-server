package com.swcampus.api.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private boolean isSseRequest() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			return false;
		}
		HttpServletRequest request = attrs.getRequest();
		String accept = request.getHeader("Accept");
		return accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
	}

	// === 비즈니스 예외 (단일 핸들러) ===

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) throws BusinessException {
		if (isSseRequest()) {
			log.debug("SSE 요청에서 비즈니스 예외 발생: {}", e.getMessage());
			throw e; // SSE 요청에서는 예외를 다시 던져 연결 종료
		}
		log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ErrorResponse.of(errorCode, e.getMessage()));
	}

	// === Validation 예외 ===

	@ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(
			jakarta.validation.ConstraintViolationException e) {
		String message = e.getConstraintViolations().stream()
				.map(violation -> violation.getMessage())
				.collect(Collectors.joining(", "));
		log.warn("Validation 실패: {}", message);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));
		log.warn("Validation 실패: {}", message);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	// === Multipart 예외 ===

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
			MissingServletRequestPartException e) {
		String partName = e.getRequestPartName();
		String message = "certificateImage".equals(partName)
				? "재직증명서는 필수입니다"
				: String.format("필수 파일이 누락되었습니다: %s", partName);
		log.warn("필수 파일 누락: {}", partName);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		log.error("IllegalArgumentException 발생: {}", e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getMessage()));
	}

	// === 데이터 무결성 예외 ===

	@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
			org.springframework.dao.DataIntegrityViolationException e) {
		String message = e.getMostSpecificCause().getMessage();

		if (message != null && message.contains("value too long")) {
			log.warn("데이터 길이 초과: {}", message);
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, "입력값이 허용된 길이를 초과했습니다"));
		}

		log.error("데이터 무결성 위반: {}", message, e);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, "데이터 저장 중 오류가 발생했습니다"));
	}

	@ExceptionHandler(FileProcessingException.class)
	public ResponseEntity<ErrorResponse> handleFileProcessingException(FileProcessingException e) {
		log.error("파일 처리 오류: {}", e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다"));
	}

	// === 인가 예외 ===

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(
			org.springframework.security.access.AccessDeniedException e)
			throws org.springframework.security.access.AccessDeniedException {
		if (isSseRequest()) {
			log.debug("SSE 요청에서 접근 거부: {}", e.getMessage());
			throw e; // SSE 요청에서는 예외를 다시 던져 연결 종료
		}
		log.warn("접근 권한 없음: {}", e.getMessage());
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(ErrorResponse.of(ErrorCode.ACCESS_DENIED, "접근 권한이 없습니다"));
	}

	@ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
			org.springframework.security.authorization.AuthorizationDeniedException e)
			throws org.springframework.security.authorization.AuthorizationDeniedException {
		if (isSseRequest()) {
			log.debug("SSE 요청에서 인가 거부: {}", e.getMessage());
			throw e; // SSE 요청에서는 예외를 다시 던져 연결 종료
		}
		log.warn("인가 거부: {}", e.getMessage());
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(ErrorResponse.of(ErrorCode.ACCESS_DENIED, "접근 권한이 없습니다"));
	}

	// === 기타 예외 ===

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) throws Exception {
		if (isSseRequest()) {
			log.debug("SSE 요청에서 예외 발생: {}", e.getMessage());
			throw e; // SSE 요청에서는 예외를 다시 던져 연결 종료
		}
		log.error("예기치 않은 오류 발생: {}", e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
