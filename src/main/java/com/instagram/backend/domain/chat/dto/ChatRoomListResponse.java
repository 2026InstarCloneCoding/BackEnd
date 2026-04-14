package com.instagram.backend.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/*
  ChatRoomListResponse — 내 채팅방 목록 조회 응답 DTO

  설계 결정 사항 (이슈 #36 참고):
    — other_members: 나를 제외한 참여자만 포함 (1:1=1명, 그룹=나 제외 전원)
    — chat_room_name: nullable. null이면 프론트가 other_members의 username으로 렌더링
    — last_message: nullable. 메시지 없는 방은 null
    — last_message.preview: TEXT 타입일 때만 본문 앞 50자, 나머지는 null
    — last_message.sender_id: 프론트가 "내가 보낸 메시지"인지 판단 → "나:" 접두사 결정
    — unread_count: chat_room_members.last_read_message_id 기준 COUNT (저장 X, 조회 시 계산)

  정렬 기준 (서비스 레이어에서 처리):
    — 1순위: last_message.sent_at DESC
    — 2순위: last_message가 null이면 chat_rooms.created_at DESC

  null 직렬화 정책:
    — Jackson 기본값(Include.ALWAYS) 사용 — null 필드도 응답 JSON에 키 = null 형태로 노출
    — 이유: 프론트가 "키 존재 여부"가 아니라 "값이 null이냐"로 분기하므로 키가 항상 있는 게 단순함
    — 예: { "chat_room_name": null, "last_message": null, "unread_count": 0 }
    — @JsonInclude를 따로 명시하지 않음 (NON_NULL 사용 안 함)
*/
@Getter
@Builder
public class ChatRoomListResponse {

    @JsonProperty("chat_room_id")
    private final Long chatRoomId;

    @JsonProperty("chat_room_type")
    private final String chatRoomType;

    @JsonProperty("chat_room_name")
    private final String chatRoomName;

    @JsonProperty("other_members")
    private final List<OtherMember> otherMembers;

    @JsonProperty("last_message")
    private final LastMessage lastMessage;

    @JsonProperty("unread_count")
    private final long unreadCount;

    /*
      OtherMember — 채팅방의 "나를 제외한" 참여자 정보
      화면에 프로필 이미지와 username을 표시하는 데 사용
    */
    @Getter
    @Builder
    public static class OtherMember {

        @JsonProperty("member_id")
        private final Long memberId;

        @JsonProperty("member_username")
        private final String memberUsername;

        @JsonProperty("profile_image_url")
        private final String profileImageUrl;
    }

    /*
      LastMessage — 채팅방의 가장 최근 메시지 요약

      preview 필드 규칙:
        — message_type == "TEXT" → message_texts.message_text_contents 앞 50자
        — message_type == "IMAGE" / "POST" / "STORY" → null
        — 프론트가 message_type 보고 "사진을 보냈습니다" 등 적절히 렌더링

      sent_at:
        — messages.created_at (BaseSoftDeleteEntity → BaseTimeEntity 상속)
    */
    @Getter
    @Builder
    public static class LastMessage {

        @JsonProperty("message_id")
        private final Long messageId;

        @JsonProperty("message_type")
        private final String messageType;

        @JsonProperty("preview")
        private final String preview;

        @JsonProperty("sender_id")
        private final Long senderId;

        @JsonProperty("sent_at")
        private final LocalDateTime sentAt;
    }
}
