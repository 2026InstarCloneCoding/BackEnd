package com.instagram.backend.domain.member.dto;

import com.instagram.backend.domain.member.entity.Gender;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

/*
  DTO (Data Transfer Object) — 클라이언트에 보낼 데이터만 담는 객체

  왜 Entity를 직접 반환하지 않는가?
  1. Entity에는 password, isDeleted 같은 내부 필드가 있음 → 노출되면 안 됨
  2. API마다 필요한 필드가 다름 → 내 프로필은 phone 포함, 다른 유저 프로필은 제외
  3. Entity가 바뀌어도 API 응답은 안 바뀌게 분리 → 유지보수 편함

  from() 정적 메서드 — Entity를 DTO로 변환하는 팩토리 메서드
  변환 로직을 DTO 안에 두면, Service 레이어가 깔끔해짐
*/
@Getter
@Builder
public class MyProfileResponse {

    private Long memberId;
    private String username;
    private String name;
    private String bio;
    private String phone;
    private Gender gender;
    private String imageUrl;
    private String imageType;
    private String imageName;
    private String imageUuid;
    private String role;
    private Long postCount;
    private Long followerCount;
    private Long followingCount;

    public static MyProfileResponse from(Member member, long postCount) {
        return MyProfileResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .bio(member.getBio())
                .phone(member.getPhone())
                .gender(member.getGender())
                .imageUrl(member.getImageUrl())
                .imageType(member.getImageType())
                .imageName(member.getImageName())
                .imageUuid(member.getImageUuid())
                .role(member.getRole())
                .postCount(postCount)
                .followerCount(member.getFollowerCount())
                .followingCount(member.getFollowingCount())
                .build();
    }
}
