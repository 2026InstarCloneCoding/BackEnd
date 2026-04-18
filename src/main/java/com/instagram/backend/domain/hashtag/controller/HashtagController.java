package com.instagram.backend.domain.hashtag.controller;

import com.instagram.backend.domain.hashtag.dto.response.HashtagResponse;
import com.instagram.backend.domain.hashtag.service.HashtagFollowService;
import com.instagram.backend.domain.hashtag.service.HashtagService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hashtags")
public class HashtagController {

    private final HashtagService hashtagService;
    private final HashtagFollowService hashtagFollowService;

    // 해시태그 단건 조회 — GET /api/hashtags/{name}
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<HashtagResponse>> getHashtag(
            @PathVariable String name) {
        return ResponseEntity.ok(
                ApiResponse.success(hashtagService.getHashtag(name), "해시태그 조회 성공")
        );
    }
    @PostMapping("/{hashtagId}/follow")
    public ResponseEntity<ApiResponse<Void>> followHashtag(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long hashtagId) {
        hashtagFollowService.followHashtag(userDetails.getMemberId(), hashtagId);
        return ResponseEntity.ok(ApiResponse.success(null, "해시태그 팔로우 완료"));
    }

    // 해시태그 언팔로우 — DELETE /api/hashtags/{hashtagId}/follow
    @DeleteMapping("/{hashtagId}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollowHashtag(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long hashtagId) {
        hashtagFollowService.unfollowHashtag(userDetails.getMemberId(), hashtagId);
        return ResponseEntity.ok(ApiResponse.success(null, "해시태그 언팔로우 완료"));
    }
}
