package com.instagram.backend.domain.member.entity;

/*
  Java Enum — 고정된 상수 값의 집합
  DB에서 gender 컬럼이 ENUM 타입이므로, Java에서도 Enum으로 정의
  M(남성), F(여성), N(선택안함) 3가지 값만 허용

  문자열 "M"이 들어오면 Gender.valueOf("M")으로 변환 가능
  잘못된 값이 오면 IllegalArgumentException 발생 → 이걸 잡아서 에러 응답
*/
public enum Gender {
    M, F, N
}
