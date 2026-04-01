package com.instagram.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    @JsonProperty("post_contents")
    private String postContents;

    @JsonProperty("comment_enabled")
    private boolean commentEnabled;
}
