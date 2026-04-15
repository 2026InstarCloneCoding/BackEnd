package com.instagram.backend.domain.message.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/*
  MessageResponse — 메시지 단건 표현 DTO

  사용처:
    — POST /api/chats/{roomId}/messages 응답 (data 필드)
    — GET  /api/chats/{roomId}/messages 응답 content 리스트의 각 아이템

  설계:
    — 공통 필드(messageId, chatRoomId, senderId, 작성자 정보, messageType, createdAt)
    — dtype별 부가 필드는 같은 DTO에 통합해서 null 허용으로 노출
    — 프론트는 messageType 값으로 어떤 필드를 읽을지 분기

  null 직렬화:
    — Jackson 기본값(ALWAYS) 사용 — 필드가 null이어도 키는 항상 응답에 포함
    — 프론트의 분기 로직이 단순해짐
*/
@Getter
@Builder
public class MessageResponse {

    @JsonProperty("message_id")
    private final Long messageId;

    @JsonProperty("chat_room_id")
    private final Long chatRoomId;

    @JsonProperty("sender_id")
    private final Long senderId;

    @JsonProperty("sender_username")
    private final String senderUsername;

    @JsonProperty("sender_profile_image_url")
    private final String senderProfileImageUrl;

    @JsonProperty("message_type")
    private final String messageType;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    // --- TEXT ---
    @JsonProperty("message_text_content")
    private final String messageTextContent;

    // --- IMAGE ---
    @JsonProperty("message_image_url")
    private final String messageImageUrl;

    @JsonProperty("message_image_type")
    private final String messageImageType;

    @JsonProperty("message_image_name")
    private final String messageImageName;

    @JsonProperty("message_image_uuid")
    private final String messageImageUuid;

    // --- POST 공유 ---
    @JsonProperty("post_id")
    private final Long postId;

    // --- STORY 공유 ---
    // 명세서(Chat.pdf) 상 요청/응답 모두 "story_visitor_id"라는 필드명을 쓰지만 실제 DB 참조는 story_id
    // 프론트와의 계약 유지를 위해 명세 그대로 따른다
    @JsonProperty("story_visitor_id")
    private final Long storyVisitorId;
}
