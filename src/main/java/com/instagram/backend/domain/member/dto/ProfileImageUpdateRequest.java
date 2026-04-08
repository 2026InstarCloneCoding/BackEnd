package com.instagram.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
  프로필 이미지 수정 요청 DTO

  @NotBlank — null, "", " " 모두 허용하지 않음
    — 이미지 필드 4개는 전부 필수(O)이므로 @NotBlank 사용
    — 프로필 수정(ProfileUpdateRequest)에서는 모든 필드가 선택이라 검증 없었지만,
      이미지는 4개가 한 세트로 움직여야 하므로 전부 필수

  @JsonProperty("image_url") — JSON의 snake_case를 Java의 camelCase에 매핑
    — 클라이언트가 보내는 JSON: { "image_url": "..." }
    — Java 필드: imageUrl
    — 이 어노테이션이 둘 사이를 연결해줌
*/
@Getter
@NoArgsConstructor
public class ProfileImageUpdateRequest {

    @NotBlank(message = "이미지 URL을 입력해주세요.")
    @JsonProperty("image_url")
    private String imageUrl;

    @NotBlank(message = "이미지 타입을 입력해주세요.")
    @JsonProperty("image_type")
    private String imageType;

    @NotBlank(message = "이미지 이름을 입력해주세요.")
    @JsonProperty("image_name")
    private String imageName;

    @NotBlank(message = "이미지 UUID를 입력해주세요.")
    @JsonProperty("image_uuid")
    private String imageUuid;
}
