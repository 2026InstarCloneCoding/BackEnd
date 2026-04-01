package com.instagram.backend.domain.member.dto;

import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

/*
  프로필 수정 응답 DTO
  API 명세에 따라 수정된 주요 필드만 반환
*/
@Getter
@Builder
public class ProfileUpdateResponse {

    private Long memberId;
    private String username;
    private String name;
    private String bio;

    public static ProfileUpdateResponse from(Member member) {
        return ProfileUpdateResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .bio(member.getBio())
                .build();
    }
}
