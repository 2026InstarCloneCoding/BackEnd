package com.instagram.backend.domain.media.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedUrlRequest {

    @NotBlank
    private String fileName;

    @NotBlank
    private String fileType;

    @NotNull
    @Positive(message = "파일 크기는 1 이상이어야 합니다.")
    private Long fileSize;

    @NotBlank
    private String uploadType;
}
