package com.instagram.backend.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/*
  ChatRoomCreateResponse — 채팅방 생성 응답 DTO

  응답 예시:
    { "room_id": 10 }

  왜 chatRoomId가 아닌 room_id로 내려주나?
    — PDF API 명세에서 room_id로 정의됨 (프론트 계약)
    — Java 필드는 camelCase(roomId), JSON은 snake_case(room_id)로 @JsonProperty로 매핑
*/
@Getter
public class ChatRoomCreateResponse {

    @JsonProperty("room_id")
    private final Long roomId;

    private ChatRoomCreateResponse(Long roomId) {
        this.roomId = roomId;
    }

    public static ChatRoomCreateResponse of(Long roomId) {
        return new ChatRoomCreateResponse(roomId);
    }
}
