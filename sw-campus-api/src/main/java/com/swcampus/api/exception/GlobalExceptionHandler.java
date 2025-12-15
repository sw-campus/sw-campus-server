package com.swcampus.api.exception;

import com.swcampus.domain.auth.exception.CertificateRequiredException;
import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.EmailNotVerifiedException;
import com.swcampus.domain.auth.exception.EmailVerificationExpiredException;
import com.swcampus.domain.auth.exception.InvalidCredentialsException;
import com.swcampus.domain.auth.exception.InvalidPasswordException;
import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.auth.exception.MailSendException;
import com.swcampus.domain.auth.exception.TokenExpiredException;
import com.swcampus.domain.certificate.exception.CertificateAlreadyExistsException;
import com.swcampus.domain.certificate.exception.CertificateLectureMismatchException;
import com.swcampus.domain.certificate.exception.CertificateNotFoundException;
import com.swcampus.domain.certificate.exception.CertificateNotVerifiedException;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.review.exception.ReviewAlreadyExistsException;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import com.swcampus.domain.review.exception.ReviewNotModifiableException;
import com.swcampus.domain.review.exception.ReviewNotOwnerException;
import com.swcampus.domain.cart.exception.AlreadyInCartException;
import com.swcampus.domain.cart.exception.CartLimitExceededException;
import com.swcampus.domain.survey.exception.SurveyAlreadyExistsException;
import com.swcampus.domain.survey.exception.SurveyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        // === Auth 관련 예외 ===

        @ExceptionHandler(DuplicateEmailException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException e) {
                log.warn("중복 이메일: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), e.getMessage()));
        }

        @ExceptionHandler(EmailVerificationExpiredException.class)
        public ResponseEntity<ErrorResponse> handleEmailVerificationExpiredException(
                        EmailVerificationExpiredException e) {
                log.warn("이메일 인증 만료: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
                log.warn("유효하지 않은 토큰: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }

        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException e) {
                log.warn("토큰 만료: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }

        @ExceptionHandler(MailSendException.class)
        public ResponseEntity<ErrorResponse> handleMailSendException(MailSendException e) {
                log.error("메일 발송 실패: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "이메일 발송에 실패했습니다"));
        }

        @ExceptionHandler(EmailNotVerifiedException.class)
        public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(EmailNotVerifiedException e) {
                log.warn("이메일 미인증: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }

        @ExceptionHandler(InvalidPasswordException.class)
        public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException e) {
                log.warn("비밀번호 정책 위반: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException e) {
                log.warn("로그인 실패: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }

        @ExceptionHandler(MemberNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleMemberNotFoundException(MemberNotFoundException e) {
                log.warn("회원 조회 실패: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }

        // === Validation 예외 ===

        @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolationException(
                        jakarta.validation.ConstraintViolationException e) {
                String message = e.getConstraintViolations().stream()
                                .map(violation -> violation.getMessage())
                                .collect(Collectors.joining(", "));
                log.warn("Validation 실패: {}", message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
                String message = e.getBindingResult().getFieldErrors().stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining(", "));
                log.warn("Validation 실패: {}", message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
        }

        // === Organization 관련 예외 ===

        @ExceptionHandler(CertificateRequiredException.class)
        public ResponseEntity<ErrorResponse> handleCertificateRequiredException(CertificateRequiredException e) {
                log.warn("재직증명서 누락: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }

        @ExceptionHandler(CertificateLectureMismatchException.class)
        public ResponseEntity<ErrorResponse> handleCertificateLectureMismatchException(
                        CertificateLectureMismatchException e) {
                log.warn("수료증 검증 실패 - 강의명 불일치: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }

        @ExceptionHandler(CertificateAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleCertificateAlreadyExistsException(
                        CertificateAlreadyExistsException e) {
                log.warn("수료증 중복 인증: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), "이미 인증된 수료증입니다"));
        }

        @ExceptionHandler(CertificateNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCertificateNotFoundException(CertificateNotFoundException e) {
                log.warn("수료증 조회 실패: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }

        // === Review 관련 예외 ===

        @ExceptionHandler(ReviewNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleReviewNotFoundException(ReviewNotFoundException e) {
                log.warn("후기 조회 실패: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }

        @ExceptionHandler(ReviewAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleReviewAlreadyExistsException(ReviewAlreadyExistsException e) {
                log.warn("후기 중복: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), e.getMessage()));
        }

        @ExceptionHandler(ReviewNotOwnerException.class)
        public ResponseEntity<ErrorResponse> handleReviewNotOwnerException(ReviewNotOwnerException e) {
                log.warn("후기 권한 없음: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        }

        @ExceptionHandler(ReviewNotModifiableException.class)
        public ResponseEntity<ErrorResponse> handleReviewNotModifiableException(ReviewNotModifiableException e) {
                log.warn("후기 수정 불가: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        }

        @ExceptionHandler(CertificateNotVerifiedException.class)
        public ResponseEntity<ErrorResponse> handleCertificateNotVerifiedException(CertificateNotVerifiedException e) {
                log.warn("수료증 미인증: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        }

        // === Cart 관련 예외 ===

        @ExceptionHandler(CartLimitExceededException.class)
        public ResponseEntity<ErrorResponse> handleCartLimitExceededException(CartLimitExceededException e) {
                log.warn("장바구니 한도 초과: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }

        @ExceptionHandler(AlreadyInCartException.class)
        public ResponseEntity<ErrorResponse> handleAlreadyInCartException(AlreadyInCartException e) {
                log.warn("장바구니 중복: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), e.getMessage()));
        }

        // === Survey 관련 예외 ===

        @ExceptionHandler(SurveyNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleSurveyNotFoundException(SurveyNotFoundException e) {
                log.warn("설문조사 조회 실패: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        }

        @ExceptionHandler(SurveyAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleSurveyAlreadyExistsException(SurveyAlreadyExistsException e) {
                log.warn("설문조사 중복: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), e.getMessage()));
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
                log.warn("잘못된 요청: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }

        // === 기타 예외 ===

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleException(Exception e) {
                log.error("예기치 않은 오류 발생: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다"));
        }
}
