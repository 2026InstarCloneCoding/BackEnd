package com.instagram.backend.domain.message.entity;

import com.instagram.backend.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(name = "message_type", nullable = false, length = 10)
    private String messageType;

    @Builder
    public Message(Long memberId, Long chatRoomId, String messageType) {
        this.memberId = memberId;
        this.chatRoomId = chatRoomId;
        this.messageType = messageType;
    }
}
