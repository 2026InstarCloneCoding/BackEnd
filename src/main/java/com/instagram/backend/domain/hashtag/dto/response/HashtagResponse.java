package com.instagram.backend.domain.hashtag.dto.response;

import com.instagram.backend.domain.hashtag.entity.Hashtag;
import lombok.Getter;

@Getter
public class HashtagResponse {
    private final Long hashtagId;
    private final String name;
    private final int postCount;

    private HashtagResponse(Long hashtagId, String name, int postCount) {
        this.hashtagId = hashtagId;
        this.name = name;
        this.postCount = postCount;
    }

    public static HashtagResponse from(Hashtag hashtag) {
        return new HashtagResponse(
                hashtag.getHashtagId(),
                hashtag.getName(),
                hashtag.getPostCount()
        );
    }
}
