package com.instagram.backend.domain.comment.controller;

import com.instagram.backend.domain.comment.dto.request.CommentCreateRequest;
import com.instagram.backend.domain.comment.dto.response.CommentCreateResponse;
import com.instagram.backend.domain.comment.dto.response.CommentListResponse;
import com.instagram.backend.domain.comment.service.CommentLikeService;
import com.instagram.backend.domain.comment.service.CommentService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    //댓글 생성 POST /api/posts/{postId}/comments
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentCreateResponse>>createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                commentService.createComment(userDetails.getMemberId(),postId,request), "댓글이 작성되었습니다."
        ));
    }

    // 댓글 삭제 — DELETE /api/posts/{postId}/comments/{commentId}
    @DeleteMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId){
        commentService.deleteComment(userDetails.getMemberId(), postId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 삭제되었습니다."));
    }

    // 댓글 목록 조회 — GET /api/posts/{postId}/comments
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        commentService.getComments(postId, cursor, size),
                        "댓글 목록 조회 성공"
                )
        );
    }

    // 댓글 좋아요  POST /api/posts/{postId}/comments/{commentId}/likes
    @PostMapping("/api/posts/{postId}/comments/{commentId}/likes")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentLikeService.likeComment(userDetails.getMemberId(), postId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글 좋아요 완료"));
    }

    // 댓글 좋아요 취소  DELETE /api/posts/{postId}/comments/{commentId}/likes
    @DeleteMapping("/api/posts/{postId}/comments/{commentId}/likes")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentLikeService.unlikeComment(userDetails.getMemberId(), postId, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "댓글 좋아요 완료"));
    }
}
