package com.instagram.backend.domain.hashtag.controller;

import com.instagram.backend.domain.hashtag.dto.response.HashtagResponse;
import com.instagram.backend.domain.hashtag.service.HashtagService;
import com.instagram.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hashtags")
public class HashtagController {

    private final HashtagService hashtagService;

    // 해시태그 단건 조회 — GET /api/hashtags/{name}
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<HashtagResponse>> getHashtag(
            @PathVariable String name) {
        return ResponseEntity.ok(
                ApiResponse.success(hashtagService.getHashtag(name), "해시태그 조회 성공")
        );
    }
}
