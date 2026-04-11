package com.instagram.backend.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Getter;

import java.util.List;

@Getter
public class StoryFeedResponse {

    @JsonProperty("member_id")
    private final Long memberId;

    private final String username;

    @JsonProperty("profile_image_url")
    private final String profileImageUrl;

    private final List<StoryResponse> stories;

    private StoryFeedResponse(Long memberId, String username, String profileImageUrl, List<StoryResponse> stories) {
        this.memberId = memberId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.stories = stories;
    }

    public static StoryFeedResponse of(Member member, List<StoryResponse> stories) {
        return new StoryFeedResponse(
                member.getMemberId(),
                member.getUsername(),
                member.getImageUrl(),
                stories
        );
    }
}
