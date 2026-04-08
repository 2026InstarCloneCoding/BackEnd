package com.instagram.backend.domain.post.controller;

import com.instagram.backend.domain.bookmark.service.BookmarkService;
import com.instagram.backend.domain.post.dto.PostCreateRequest;
import com.instagram.backend.domain.post.dto.PostCreateResponse;
import com.instagram.backend.domain.post.dto.PostResponse;
import com.instagram.backend.domain.post.dto.PostUpdateRequest;
import com.instagram.backend.domain.post.service.PostLikeService;
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
    private final PostLikeService postLikeService;
    private final BookmarkService bookmarkService;

    // 게시물 단건 조회 — GET /api/posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.getPost(userDetails.getMemberId(), postId), "게시물 조회 성공")
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
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request) {
        postService.updatePost(userDetails.getMemberId(), postId, request);
        return ResponseEntity.ok(
                ApiResponse.success(null, "게시물이 수정되었습니다.")
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


    // 게시물 좋아요 — POST /api/posts/{postId}/like
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {
        postLikeService.likePost(userDetails.getMemberId(),postId);
        return ResponseEntity.ok(ApiResponse.success(null, "좋아요가 완료되었습니다."));
    }

    // 게시물 좋아요 취소 — DELETE /api/posts/{postId}/like
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {
        postLikeService.unlikePost(userDetails.getMemberId(),postId);
        return ResponseEntity.ok(ApiResponse.success(null, "좋아요가 취소되었습니다."));
    }


    // 게시물 저장 — POST /api/posts/{postId}/bookmark
    @PostMapping("/{postId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> bookmarkPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {
        bookmarkService.bookmarkPost(userDetails.getMemberId(),postId);
        return ResponseEntity.ok(ApiResponse.success(null, "게시물이 저장되었습니다."));
    }

    // 게시물 저장 취소 — DELETE /api/posts/{postId}/bookmark
    @DeleteMapping("/{postId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> unbookmarkPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId){
        bookmarkService.unbookmarkPost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(ApiResponse.success(null, "게시물 저장이 취소되었습니다."));
    }
}
