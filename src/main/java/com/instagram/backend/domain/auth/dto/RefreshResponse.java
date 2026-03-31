package com.instagram.backend.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshResponse {

    private String accessToken;
    private long expiresIn;
}
