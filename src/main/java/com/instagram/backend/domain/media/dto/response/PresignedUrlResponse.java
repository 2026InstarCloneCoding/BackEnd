package com.instagram.backend.domain.media.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresignedUrlResponse {
    private final String uploadUrl;
    private final String mediaUrl;
    private final String mediaUuid;
    private final long expiresIn;
}
