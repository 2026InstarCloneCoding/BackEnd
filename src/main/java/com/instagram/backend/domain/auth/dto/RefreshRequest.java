package com.instagram.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshRequest {

    @NotBlank(message = "refresh_token을 입력해주세요.")
    private String refreshToken;
}
