package com.instagram.backend.domain.media.controller;

import com.instagram.backend.domain.media.dto.request.PresignedUrlRequest;
import com.instagram.backend.domain.media.dto.response.PresignedUrlResponse;
import com.instagram.backend.domain.media.service.MediaService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    // Presigned URL 발급 — POST /api/media/upload
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.generatePresignedUrl(request), "업로드 URL이 발급되었습니다."));
    }
}
