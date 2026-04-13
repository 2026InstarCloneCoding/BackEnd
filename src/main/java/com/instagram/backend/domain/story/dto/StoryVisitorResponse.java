package com.instagram.backend.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.story.entity.StoryVisitor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoryVisitorResponse {

    @JsonProperty("member_id")
    private final Long memberId;

    private final String username;

    @JsonProperty("profile_image_url")
    private final String profileImageUrl;

    @JsonProperty("visited_at")
    private final LocalDateTime visitedAt;

    private StoryVisitorResponse(Long memberId, String username, String profileImageUrl, LocalDateTime visitedAt) {
        this.memberId = memberId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.visitedAt = visitedAt;
    }

    public static StoryVisitorResponse of(StoryVisitor visitor, Member member) {
        return new StoryVisitorResponse(
                member.getMemberId(),
                member.getUsername(),
                member.getImageUrl(),
                visitor.getCreatedAt()
        );
    }
}
