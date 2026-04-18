package com.instagram.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 - 잘못된 요청
    EXCEED_IMAGE_LIMIT(400, "이미지는 최대 10개까지 첨부 가능합니다."),
    EXCEED_CONTENT_LENGTH(400, "게시물 내용은 2200자 이내로 입력해주세요."),
    INVALID_TAG_USERNAME(400, "태그한 사용자를 찾을 수 없습니다."),
    INVALID_LIMIT(400, "limit 값이 올바르지 않습니다."),
    INVALID_CURSOR(400, "cursor 값이 올바르지 않습니다."),
    INVALID_EMAIL_FORMAT(400, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(400, "비밀번호는 8자 이상이어야 합니다."),
    INVALID_USERNAME_FORMAT(400, "유저네임은 영문, 숫자, _만 사용 가능합니다."),
    MISSING_REQUIRED_FIELD(400, "필수 항목을 모두 입력해주세요."),
    INVALID_BIO_LENGTH(400, "소개글은 160자 이내로 입력해주세요."),
    INVALID_GENDER_VALUE(400, "성별 값이 올바르지 않습니다. (MALE/FEMALE/OTHER)"),
    INVALID_IMAGE_TYPE(400, "지원하지 않는 이미지 형식입니다."),
    EXCEED_COMMENT_LENGTH(400, "댓글 내용은 500자 이내로 입력해주세요."),
    INVALID_DTYPE(400, "올바르지 않은 메시지 타입입니다."),
    MISSING_MESSAGE_CONTENT(400, "메시지 내용을 입력해주세요."),

    // 401 - 인증 오류
    UNAUTHORIZED(401, "로그인이 필요합니다."),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "토큰이 만료되었습니다. 다시 로그인해주세요."),

    // 403 - 권한 오류
    AUTH_ACCESS_DENIED(403, "접근 권한이 없습니다."),
    FORBIDDEN(403, "본인의 게시물만 수정/삭제할 수 있습니다."),
    STORY_ACCESS_DENIED(403, "본인의 스토리만 수정/삭제할 수 있습니다."),
    BLOCKED_MEMBER(403, "차단된 사용자의 스토리입니다."),
    COMMENT_DISABLED(403, "댓글이 허용되지 않는 게시물입니다."),
    COMMENT_FORBIDDEN(403, "댓글 작성자 또는 게시물 주인만 삭제할 수 있습니다."),
    CHAT_ROOM_FORBIDDEN(403, "해당 채팅방에 접근 권한이 없습니다."),

    CANNOT_FOLLOW_SELF(400, "자기 자신을 팔로우할 수 없습니다."),
    SELF_CHAT_NOT_ALLOWED(400, "자기 자신과 채팅방을 만들 수 없습니다."),

    // 404 - 리소스 없음
    MEMBER_NOT_FOUND(404, "존재하지 않는 계정입니다."),
    POST_NOT_FOUND(404, "존재하지 않는 게시물입니다."),
    LIKE_NOT_FOUND(404, "좋아요 내역이 존재하지 않습니다."),
    BOOKMARK_NOT_FOUND(404, "저장 내역이 존재하지 않습니다."),
    FOLLOW_NOT_FOUND(404, "팔로우 관계가 존재하지 않습니다."),
    STORY_NOT_FOUND(404, "존재하지 않는 스토리입니다."),
    COMMENT_NOT_FOUND(404, "존재하지 않는 댓글입니다."),
    COMMENT_LIKE_NOT_FOUND(404, "댓글 좋아요 내역이 존재하지 않습니다."),
    ROOM_NOT_FOUND(404, "존재하지 않는 채팅방입니다."),
    HASHTAG_NOT_FOUND(404, "존재하지 않는 해시태그입니다."),
    HASHTAG_FOLLOW_NOT_FOUND(404, "해시태그 팔로우 내역이 존재하지 않습니다."),

    // 409 - 중복
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다."),
    DUPLICATE_USERNAME(409, "이미 사용 중인 유저네임입니다."),
    ALREADY_LIKED(409, "이미 좋아요한 게시물입니다."),
    ALREADY_BOOKMARKED(409, "이미 저장한 게시물입니다."),
    ALREADY_FOLLOWING(409, "이미 팔로우한 사용자입니다."),
    ALREADY_COMMENT_LIKED(409, "이미 좋아요한 댓글입니다."),
    ROOM_ALREADY_EXISTS(409, "이미 존재하는 채팅방입니다."),
    ALREADY_HASHTAG_FOLLOWED(409, "이미 팔로우한 해시태그입니다."),

    // 500 - 서버 오류
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
