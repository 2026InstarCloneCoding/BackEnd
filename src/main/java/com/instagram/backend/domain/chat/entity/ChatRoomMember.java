package com.instagram.backend.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomMemberId;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime joinAt;

    private Long lastReadMessageId;

    @Builder
    public ChatRoomMember(Long chatRoomId, Long memberId, LocalDateTime joinAt) {
        this.chatRoomId = chatRoomId;
        this.memberId = memberId;
        this.joinAt = joinAt;
    }

    /*
      읽음 처리 — lastReadMessageId 갱신

      목적: 사용자가 특정 메시지까지 읽었음을 기록
        — 이 값은 GET /api/chats의 unread_count 계산에 사용됨
        — MessageRepository.countByChatRoomIdAndMessageIdGreaterThan(roomId, lastReadMessageId)

      방어 로직: 현재 값보다 작은(= 더 오래된) messageId로 갱신 시도하면 무시
        — 예: 최신 메시지 50까지 읽었는데 PATCH로 30을 보내면 50 유지
        — 프론트가 실수로 과거 메시지를 보내도 unread_count가 늘어나지 않도록 보호
    */
    public void updateLastReadMessageId(Long messageId) {
        if (messageId == null) return;
        if (this.lastReadMessageId == null || messageId > this.lastReadMessageId) {
            this.lastReadMessageId = messageId;
        }
    }
}
