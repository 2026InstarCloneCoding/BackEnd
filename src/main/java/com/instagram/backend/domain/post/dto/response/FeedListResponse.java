package com.instagram.backend.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedListResponse {

    private final List<FeedResponse> posts;
    private final Long nextCursor;
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
