package com.instagram.backend.domain.story.controller;

import com.instagram.backend.domain.story.dto.*;
import com.instagram.backend.domain.story.service.StoryService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoryCreateResponse>> createStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody StoryCreateRequest request) {
        StoryCreateResponse response = storyService.createStory(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "스토리가 업로드되었습니다."));
    }

    // /feed 를 /{storyId} 보다 먼저 선언해야 경로 충돌 방지
    @GetMapping("feed")
    public ResponseEntity<ApiResponse<List<StoryFeedResponse>>> getStoryFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<StoryFeedResponse> response = storyService.getStoryFeed(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response, "스토리 피드 조회 성공"));
    }

    @GetMapping("/{storyId}")
    public ResponseEntity<ApiResponse<StoryResponse>> getStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storyId) {
        StoryResponse response = storyService.getStory(userDetails.getMemberId(), storyId);
        return ResponseEntity.ok(ApiResponse.success(response, "스토리 조회 성공"));
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<ApiResponse<Void>> deleteStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storyId) {
        storyService.deleteStory(userDetails.getMemberId(), storyId);
        return ResponseEntity.ok(ApiResponse.success(null, "스토리가 삭제되었습니다."));
    }

    @GetMapping("/{storyId}/visitors")
    public ResponseEntity<ApiResponse<List<StoryVisitorResponse>>> getStoryVisitors(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long storyId) {
        List<StoryVisitorResponse> response = storyService.getStoryVisitors(userDetails.getMemberId(), storyId);
        return ResponseEntity.ok(ApiResponse.success(response, "방문자 목록 조회 성공"));
    }
}
