package com.instagram.backend.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageLikeId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long messageId;

    @Builder
    public MessageLike(Long memberId, Long messageId) {
        this.memberId = memberId;
        this.messageId = messageId;
    }
}
