package com.instagram.backend.domain.comment.controller;

import com.instagram.backend.domain.comment.dto.request.CommentCreateRequest;
import com.instagram.backend.domain.comment.dto.response.CommentCreateResponse;
import com.instagram.backend.domain.comment.service.CommentService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

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



}
