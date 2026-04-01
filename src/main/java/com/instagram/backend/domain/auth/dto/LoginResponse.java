package com.instagram.backend.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private MemberInfo member;

    @Getter
    @Builder
    public static class MemberInfo {
        private Long memberId;
        private String memberUsername;
        private String memberImageUrl; // 추후 Member 엔티티에 필드 추가 시 연동
    }
}
