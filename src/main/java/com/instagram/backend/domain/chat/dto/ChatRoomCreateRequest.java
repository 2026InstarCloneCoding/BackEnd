package com.instagram.backend.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/*
  ChatRoomCreateRequest — 채팅방 생성 요청 DTO

  요청 예시:
    { "member_ids": [2, 3] }

  @NotEmpty
    — null이거나 빈 리스트이면 검증 실패 → 400 MISSING_REQUIRED_FIELD
    — GlobalExceptionHandler가 MethodArgumentNotValidException을 변환하여 에러 응답 반환

  @NoArgsConstructor
    — Jackson이 JSON → DTO로 역직렬화할 때 기본 생성자가 필요함
    — @Getter와 함께 있으면 Setter 없이도 Jackson이 필드에 값 주입 가능 (리플렉션 사용)
*/
@Getter
@NoArgsConstructor
public class ChatRoomCreateRequest {

    @NotEmpty(message = "참여자를 1명 이상 지정해주세요.")
    @JsonProperty("member_ids")
    private List<Long> memberIds;
}
