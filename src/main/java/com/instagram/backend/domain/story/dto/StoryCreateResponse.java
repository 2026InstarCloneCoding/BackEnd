package com.instagram.backend.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.story.entity.Story;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoryCreateResponse {

    @JsonProperty("story_id")
    private final Long storyId;

    @JsonProperty("story_upload_date")
    private final LocalDateTime storyUploadDate;

    private StoryCreateResponse(Long storyId, LocalDateTime storyUploadDate) {
        this.storyId = storyId;
        this.storyUploadDate = storyUploadDate;
    }

    public static StoryCreateResponse from(Story story) {
        return new StoryCreateResponse(story.getStoryId(), story.getCreatedAt());
    }
}
