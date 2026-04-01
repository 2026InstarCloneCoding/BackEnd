package com.instagram.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 - 잘못된 요청
    INVALID_EMAIL_FORMAT(400, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(400, "비밀번호는 8자 이상이어야 합니다."),
    INVALID_USERNAME_FORMAT(400, "유저네임은 영문, 숫자, _만 사용 가능합니다."),
    MISSING_REQUIRED_FIELD(400, "필수 항목을 모두 입력해주세요."),

    // 401 - 인증 오류
    UNAUTHORIZED(401, "로그인이 필요합니다."),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "토큰이 만료되었습니다. 다시 로그인해주세요."),

    // 403 - 권한 오류
    AUTH_ACCESS_DENIED(403, "접근 권한이 없습니다."),

    // 404 - 리소스 없음
    MEMBER_NOT_FOUND(404, "존재하지 않는 계정입니다."),

    // 409 - 중복
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다."),
    DUPLICATE_USERNAME(409, "이미 사용 중인 유저네임입니다."),

    // 500 - 서버 오류
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
