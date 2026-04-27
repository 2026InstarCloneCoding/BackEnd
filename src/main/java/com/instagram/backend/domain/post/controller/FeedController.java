package com.instagram.backend.domain.post.controller;

import com.instagram.backend.domain.post.dto.response.FeedListResponse;
import com.instagram.backend.domain.post.service.FeedService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    // 홈 피드 조회 — GET /api/feed?cursor={cursor}&size={size}
    @GetMapping
    public ResponseEntity<ApiResponse<FeedListResponse>> getFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        feedService.getFeed(userDetails.getMemberId(), cursor, size),
                        "피드 조회 성공"
                )
        );
    }
}
