package com.instagram.backend.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_texts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageTextId;

    @Column(nullable = false)
    private Long messageId;

    @Column(name = "message_text_contents", columnDefinition = "TEXT")
    private String messageTextContents;

    @Builder
    public MessageText(Long messageId, String messageTextContents) {
        this.messageId = messageId;
        this.messageTextContents = messageTextContents;
    }
}
