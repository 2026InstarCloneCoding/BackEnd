package com.instagram.backend.domain.member.dto;

import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

/*
  다른 유저 프로필 조회용 DTO
  MyProfileResponse와 비교:
  — phone, gender 제외 (타인의 개인정보)
  — isFollowing 추가 (내가 이 유저를 팔로우하고 있는지)
*/
@Getter
@Builder
public class UserProfileResponse {

    private Long memberId;
    private String username;
    private String name;
    private String bio;
    private String imageUrl;
    private String imageType;
    private String imageName;
    private String imageUuid;
    private Long postCount;
    private Long followerCount;
    private Long followingCount;
    private Boolean isFollowing;

    public static UserProfileResponse from(Member member, long postCount, boolean isFollowing) {
        return UserProfileResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .bio(member.getBio())
                .imageUrl(member.getImageUrl())
                .imageType(member.getImageType())
                .imageName(member.getImageName())
                .imageUuid(member.getImageUuid())
                .postCount(postCount)
                .followerCount(member.getFollowerCount())
                .followingCount(member.getFollowingCount())
                .isFollowing(isFollowing)
                .build();
    }
}
