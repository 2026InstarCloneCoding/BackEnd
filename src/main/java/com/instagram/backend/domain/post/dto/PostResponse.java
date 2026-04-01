package com.instagram.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {

    @JsonProperty("post_id")
    private final Long postId;

    @JsonProperty("member_id")
    private final Long memberId;

    @JsonProperty("member_username")
    private final String memberUsername;

    @JsonProperty("post_contents")
    private final String postContents;

    @JsonProperty("comment_enabled")
    private final boolean commentEnabled;

    @JsonProperty("like_count")
    private final int likeCount;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    public static PostResponse from(Post post) {
        return new PostResponse(post);
    }

    private PostResponse(Post post) {
        this.postId = post.getPostId();
        this.memberId = post.getMember().getId();
        this.memberUsername = post.getMember().getMemberUsername();
        this.postContents = post.getContents();
        this.commentEnabled = post.isCommentEnabled();
        this.likeCount = post.getLikeCount();
        this.createdAt = post.getCreatedAt();
    }
}
