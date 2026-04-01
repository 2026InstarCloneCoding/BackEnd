package com.instagram.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostTagRequest {
    @JsonProperty("post_tag_username")
    private String postTagUsername;

    @JsonProperty("post_tag_x")
    private Integer postTagX;

    @JsonProperty("post_tag_y")
    private Integer postTagY;

}
