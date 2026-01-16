package com.swcampus.shared.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드를 정의하는 Enum 클래스입니다.
 * <p>
 * 코드 접두사 규칙:
 * <ul>
 *     <li>C: Common (공통)</li>
 *     <li>M: Member (회원)</li>
 *     <li>A: Auth (인증)</li>
 *     <li>O: Organization (기관)</li>
 *     <li>T: Certificate (수료증)</li>
 *     <li>R: Review (후기)</li>
 *     <li>B: Cart (장바구니)</li>
 *     <li>S: Survey (설문)</li>
 *     <li>F: File/Storage (파일)</li>
 *     <li>L: Lecture (강의)</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// Common (C)
	INVALID_INPUT(400, "C001", "잘못된 입력입니다"),
	INTERNAL_SERVER_ERROR(500, "C002", "서버 내부 오류입니다"),
	RATE_LIMIT_EXCEEDED(429, "C003", "요청 한도를 초과했습니다"),
	RESOURCE_NOT_FOUND(404, "C004", "리소스를 찾을 수 없습니다"),

	// Member (M)
	MEMBER_NOT_FOUND(404, "M001", "회원을 찾을 수 없습니다"),
	DUPLICATE_EMAIL(409, "M002", "이미 존재하는 이메일입니다"),
	DUPLICATE_NICKNAME(409, "M003", "이미 존재하는 닉네임입니다"),
	ADMIN_NOT_FOUND(500, "M004", "관리자를 찾을 수 없습니다"),

	// Auth (A)
	INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다"),
	TOKEN_EXPIRED(401, "A002", "토큰이 만료되었습니다"),
	INVALID_CREDENTIALS(401, "A003", "이메일 또는 비밀번호가 올바르지 않습니다"),
	EMAIL_NOT_VERIFIED(400, "A004", "이메일 인증이 필요합니다"),
	EMAIL_VERIFICATION_EXPIRED(400, "A005", "이메일 인증이 만료되었습니다"),
	INVALID_PASSWORD(400, "A006", "비밀번호 정책을 위반했습니다"),
	MAIL_SEND_FAILED(500, "A007", "이메일 발송에 실패했습니다"),
	OAUTH_AUTHENTICATION_FAILED(401, "A008", "소셜 로그인 인증에 실패했습니다"),

	// Organization (O)
	ORGANIZATION_NOT_FOUND(404, "O001", "기관을 찾을 수 없습니다"),
	ORGANIZATION_NOT_APPROVED(403, "O002", "승인되지 않은 기관입니다"),
	DUPLICATE_ORGANIZATION_MEMBER(409, "O003", "이미 가입된 기관 회원입니다"),
	CERTIFICATE_REQUIRED(400, "O004", "재직증명서가 필요합니다"),

	// Certificate (T)
	CERTIFICATE_NOT_FOUND(404, "T001", "수료증을 찾을 수 없습니다"),
	CERTIFICATE_ALREADY_EXISTS(409, "T002", "이미 인증된 수료증입니다"),
	CERTIFICATE_NOT_VERIFIED(403, "T003", "인증되지 않은 수료증입니다"),
	CERTIFICATE_LECTURE_MISMATCH(400, "T004", "수료증 강의명이 일치하지 않습니다"),
	CERTIFICATE_NOT_EDITABLE(403, "T005", "수정할 수 없는 수료증입니다"),

	// Review (R)
	REVIEW_NOT_FOUND(404, "R001", "후기를 찾을 수 없습니다"),
	REVIEW_ALREADY_EXISTS(409, "R002", "이미 작성한 후기가 있습니다"),
	REVIEW_NOT_OWNER(403, "R003", "후기 수정 권한이 없습니다"),
	REVIEW_NOT_MODIFIABLE(403, "R004", "수정할 수 없는 후기입니다"),

	// Cart (B)
	ALREADY_IN_CART(409, "B001", "이미 장바구니에 있습니다"),
	CART_LIMIT_EXCEEDED(400, "B002", "장바구니 한도를 초과했습니다"),

	// Survey (S)
	SURVEY_NOT_FOUND(404, "S001", "설문조사를 찾을 수 없습니다"),
	SURVEY_ALREADY_EXISTS(409, "S002", "이미 응답한 설문조사입니다"),
	BASIC_SURVEY_REQUIRED(400, "S003", "기초 설문을 먼저 완료해야 합니다"),
	APTITUDE_TEST_REQUIRED(400, "S004", "성향 테스트를 먼저 완료해야 합니다"),
	SURVEY_QUESTION_SET_NOT_FOUND(404, "S005", "문항 세트를 찾을 수 없습니다"),
	QUESTION_SET_NOT_EDITABLE(403, "S006", "발행된 문항 세트는 수정/삭제할 수 없습니다"),
	SURVEY_QUESTION_NOT_FOUND(404, "S007", "문항을 찾을 수 없습니다"),
	SURVEY_OPTION_NOT_FOUND(404, "S008", "선택지를 찾을 수 없습니다"),
	INVALID_APTITUDE_TEST_ANSWERS(400, "S009", "성향 테스트 응답이 유효하지 않습니다"),

	// Storage/File (F)
	STORAGE_KEY_NOT_FOUND(404, "F001", "파일을 찾을 수 없습니다"),
	STORAGE_ACCESS_DENIED(403, "F002", "파일 접근 권한이 없습니다"),
	STORAGE_BATCH_LIMIT_EXCEEDED(400, "F003", "파일 업로드 한도를 초과했습니다"),
	INVALID_STORAGE_CATEGORY(400, "F004", "잘못된 파일 카테고리입니다"),

	// Lecture (L)
	LECTURE_NOT_MODIFIABLE(403, "L001", "수정할 수 없는 강의입니다");

	private final int status;
	private final String code;
	private final String message;
}
