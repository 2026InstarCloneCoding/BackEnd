package com.instagram.backend.domain.post.controller;

import com.instagram.backend.domain.post.dto.PostCreateRequest;
import com.instagram.backend.domain.post.dto.PostCreateResponse;
import com.instagram.backend.domain.post.dto.PostResponse;
import com.instagram.backend.domain.post.dto.PostUpdateRequest;
import com.instagram.backend.domain.post.service.PostService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시물 단건 조회 — GET /api/posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @PathVariable Long postId) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.getPost(postId), "게시물 조회 성공")
        );
    }

    // 게시물 생성 — POST /api/posts (201 반환)
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(postService.createPost(userDetails.getMemberId(), request), "게시물이 등록되었습니다.")
        );
    }

    // 게시물 수정 — PATCH /api/posts/{postId}
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.updatePost(userDetails.getMemberId(), postId, request), "게시물이 수정되었습니다.")
        );
    }

    // 게시물 삭제 — DELETE /api/posts/{postId}
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {
        postService.deletePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "게시물이 삭제되었습니다.")
        );
    }
}
