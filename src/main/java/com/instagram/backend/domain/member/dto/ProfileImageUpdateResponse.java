package com.instagram.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

/*
  프로필 이미지 수정 응답 DTO

  API 명세서에 따르면 응답은 변경된 이미지 정보 4개만 반환
  — member_id나 username 같은 프로필 정보는 포함하지 않음
  — 클라이언트가 "이미지가 잘 바뀌었구나"만 확인하면 되기 때문

  static factory method: from(Member)
    — Entity → DTO 변환을 DTO 안에서 처리하는 패턴
    — Service가 Entity의 내부 구조를 몰라도 됨 (캡슐화)
*/
@Getter
@Builder
public class ProfileImageUpdateResponse {

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("image_type")
    private String imageType;

    @JsonProperty("image_name")
    private String imageName;

    @JsonProperty("image_uuid")
    private String imageUuid;

    public static ProfileImageUpdateResponse from(Member member) {
        return ProfileImageUpdateResponse.builder()
                .imageUrl(member.getImageUrl())
                .imageType(member.getImageType())
                .imageName(member.getImageName())
                .imageUuid(member.getImageUuid())
                .build();
    }
}
