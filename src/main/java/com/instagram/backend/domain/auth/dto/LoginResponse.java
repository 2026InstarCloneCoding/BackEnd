package com.instagram.backend.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private long expiresIn;
    private MemberInfo member;

    @Getter
    @Builder
    public static class MemberInfo {
        private Long memberId;
        private String username;
        private String imageUrl;
    }
}
