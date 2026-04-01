package com.instagram.backend.domain.member.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
  요청 DTO — 클라이언트가 보낸 JSON을 Java 객체로 변환

  @Size(max = 160) — Bean Validation 어노테이션
  Spring이 자동으로 검증하고, 초과 시 MethodArgumentNotValidException 발생
  → GlobalExceptionHandler가 잡아서 에러 응답으로 변환

  @Getter — Jackson(JSON 라이브러리)이 역직렬화할 때 getter가 필요
  @NoArgsConstructor — Jackson이 JSON → 객체 변환 시 기본 생성자가 필요
*/
@Getter
@NoArgsConstructor
public class ProfileUpdateRequest {

    @Size(max = 50, message = "이름은 50자 이내로 입력해주세요.")
    private String name;

    @Size(max = 160, message = "소개글은 160자 이내로 입력해주세요.")
    private String bio;

    @Size(max = 20, message = "전화번호는 20자 이내로 입력해주세요.")
    private String phone;

    // gender는 String으로 받아서 Service에서 Enum 변환 + 검증
    private String gender;
}
