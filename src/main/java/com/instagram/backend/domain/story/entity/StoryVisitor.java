package com.instagram.backend.domain.story.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "story_visitors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryVisitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storyVisitorId;

    @Column(nullable = false)
    private Long storyId;

    @Column(nullable = false)
    private Long memberId;

    @Builder
    public StoryVisitor(Long storyId, Long memberId) {
        this.storyId = storyId;
        this.memberId = memberId;
    }
}
