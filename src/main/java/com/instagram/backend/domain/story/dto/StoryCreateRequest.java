package com.instagram.backend.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoryCreateRequest {

    @JsonProperty("story_image_url")
    private String imageUrl;

    @JsonProperty("story_image_type")
    private String imageType;

    @JsonProperty("story_image_name")
    private String imageName;

    @JsonProperty("story_image_uuid")
    private String imageUuid;
}
