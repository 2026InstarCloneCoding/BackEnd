package com.instagram.backend.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.story.entity.Story;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoryResponse {

    @JsonProperty("story_id")
    private final Long storyId;

    @JsonProperty("member_id")
    private final Long memberId;

    @JsonProperty("image_url")
    private final String imageUrl;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    @JsonProperty("expires_at")
    private final LocalDateTime expiresAt;

    private StoryResponse(Long storyId, Long memberId, String imageUrl,
                          LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.storyId = storyId;
        this.memberId = memberId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static StoryResponse from(Story story) {
        return new StoryResponse(
                story.getStoryId(),
                story.getMemberId(),
                story.getImageUrl(),
                story.getCreatedAt(),
                story.getExpiresAt()
        );
    }
}
