package com.instagram.backend.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessagePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messagePostId;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false)
    private Long postId;

    @Builder
    public MessagePost(Long messageId, Long postId) {
        this.messageId = messageId;
        this.postId = postId;
    }
}
