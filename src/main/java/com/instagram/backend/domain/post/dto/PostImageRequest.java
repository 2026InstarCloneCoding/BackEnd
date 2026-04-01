package com.instagram.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostImageRequest {
    @JsonProperty("post_image_url")
    private String postImageUrl;

    @JsonProperty("post_image_type")
    private String postImageType;

    @JsonProperty("post_image_name")
    private String postImageName;

    @JsonProperty("post_image_uuid")
    private String postImageUuid;

    @JsonProperty("post_image_alt_text")
    private String postImageAltText;

    private List<PostTagRequest> tags;

}
