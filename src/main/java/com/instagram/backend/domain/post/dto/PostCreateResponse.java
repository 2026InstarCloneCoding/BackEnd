package com.instagram.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostCreateResponse {
    @JsonProperty("post_id")
    private final Long postId;

    @JsonProperty("post_upload_date")
    private final LocalDateTime postUploadDate;

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(post);
    }

    private PostCreateResponse(Post post) {
        this.postId = post.getPostId();
        this.postUploadDate = post.getCreatedAt();
    }
}
