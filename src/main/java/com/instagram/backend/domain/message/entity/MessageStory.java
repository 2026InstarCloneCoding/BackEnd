package com.instagram.backend.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_stories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageStoriesId;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false)
    private Long storyId;

    @Builder
    public MessageStory(Long messageId, Long storyId) {
        this.messageId = messageId;
        this.storyId = storyId;
    }
}
