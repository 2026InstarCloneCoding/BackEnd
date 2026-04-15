package com.instagram.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest {
    @Size(max = 2200)
    @JsonProperty("post_contents")
    private String postContents;

    @JsonProperty("comment_enabled")
    private boolean commentEnabled;

    private List<PostImageRequest> images;


}
