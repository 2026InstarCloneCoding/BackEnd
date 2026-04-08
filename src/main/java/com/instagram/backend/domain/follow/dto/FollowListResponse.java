package com.instagram.backend.domain.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

/*
  FollowListResponse — 팔로워/팔로잉 목록의 각 항목 DTO

  팔로워 목록과 팔로잉 목록 모두 같은 형태로 응답
    — 유저의 기본 정보만 보여줌 (member_id, username, name, image_url)
    — 프로필의 모든 정보(bio, phone 등)는 불필요 → 필요한 것만 담는 것이 DTO의 역할

  is_following:
    — "내가 이 유저를 팔로우하고 있는지" 여부
    — 팔로워 목록에서 "맞팔하기" 버튼을 보여줄지 결정할 때 사용
    — 이미 팔로우 중이면 "팔로잉" 표시, 아니면 "팔로우" 버튼 표시
*/
@Getter
@Builder
public class FollowListResponse {

    @JsonProperty("member_id")
    private Long memberId;

    private String username;

    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_following")
    private boolean isFollowing;

    public static FollowListResponse of(Member member, boolean isFollowing) {
        return FollowListResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .imageUrl(member.getImageUrl())
                .isFollowing(isFollowing)
                .build();
    }
}
