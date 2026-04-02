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
}
