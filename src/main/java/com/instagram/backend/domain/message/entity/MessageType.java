package com.instagram.backend.domain.message.entity;

/*
  MessageType — 메시지 타입 enum

  설계 배경:
    — Chat 도메인에서 "메시지 타입"이 여러 서비스(MessageService, ChatService)에 걸쳐 매직 스트링으로 존재했음
    — 각 서비스가 별도로 private static final String으로 상수를 선언하면 오타·누락이 한쪽에서만 발생할 수 있음
    — enum으로 통합해서 "TEXT/IMAGE/POST/STORY 외에는 존재할 수 없음"을 컴파일 타임에 보장

  DB 컬럼 타입:
    — Message 엔티티의 messageType 필드는 현재 String (DB 컬럼 message_type VARCHAR)
    — 기존 데이터 호환을 위해 엔티티는 String 유지, 서비스 레이어에서 enum.name()으로 비교·저장
    — 향후 @Enumerated(EnumType.STRING)으로 엔티티 필드를 enum으로 바꾸는 리팩토링 가능

  isValid(String):
    — 클라이언트가 보낸 dtype 문자열이 유효한 enum 값인지 검증할 때 사용
    — 유효하지 않으면 ErrorCode.INVALID_DTYPE 던지는 용도
*/
public enum MessageType {
    TEXT,
    IMAGE,
    POST,
    STORY;

    public static boolean isValid(String value) {
        if (value == null) return false;
        for (MessageType type : values()) {
            if (type.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
