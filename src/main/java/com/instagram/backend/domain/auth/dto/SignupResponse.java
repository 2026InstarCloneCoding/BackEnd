package com.instagram.backend.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {

    private Long memberId;
    private String memberUsername;
}
