package com.instagram.backend.domain.search.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

/*
  UserSearchResponse — 유저 검색 응답 DTO

  설계:
    — 명세서(Search.pdf)의 응답 구조를 그대로 반영
    — members 리스트 안에 각 유저의 기본 정보 + is_following 포함
    — is_following: 내가 해당 유저를 팔로우하고 있는지 여부
      → 검색 결과 화면에서 "팔로우" / "팔로잉" 버튼 상태를 결정할 때 사용

  of() 팩토리 메서드:
    — Member 엔티티 리스트 + 내가 팔로우 중인 ID Set을 받아 DTO 변환
    — Set.contains()로 O(1) 팔로우 여부 판단 (N+1 회피)
*/
@Getter
@Builder
public class UserSearchResponse {

    @JsonProperty("members")
    private final List<UserInfo> members;

    @Getter
    @Builder
    public static class UserInfo {

        @JsonProperty("member_id")
        private final Long memberId;

        @JsonProperty("member_username")
        private final String memberUsername;

        @JsonProperty("member_name")
        private final String memberName;

        @JsonProperty("member_image_url")
        private final String memberImageUrl;

        @JsonProperty("member_image_uuid")
        private final String memberImageUuid;

        @JsonProperty("is_following")
        private final boolean isFollowing;
    }

    public static UserSearchResponse of(List<Member> members, Set<Long> followingIds) {
        List<UserInfo> userInfos = members.stream()
                .map(m -> UserInfo.builder()
                        .memberId(m.getMemberId())
                        .memberUsername(m.getMemberUsername())
                        .memberName(m.getName())
                        .memberImageUrl(m.getImageUrl())
                        .memberImageUuid(m.getImageUuid())
                        .isFollowing(followingIds.contains(m.getMemberId()))
                        .build())
                .toList();

        return UserSearchResponse.builder()
                .members(userInfos)
                .build();
    }
}
