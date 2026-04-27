package com.instagram.backend.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class FeedListResponse {

    @JsonProperty("posts")
    private final List<FeedResponse> posts;

    @JsonProperty("next_cursor")
    private final Long nextCursor;

    @JsonProperty("has_more")
    private final boolean hasMore;

    private FeedListResponse(List<FeedResponse> posts, Long nextCursor, boolean hasMore) {
        this.posts = posts;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }

    public static FeedListResponse of(List<FeedResponse> posts, Long nextCursor, boolean hasMore) {
        return new FeedListResponse(posts, nextCursor, hasMore);
    }

    public static FeedListResponse empty() {
        return new FeedListResponse(List.of(), null, false);
    }
}
