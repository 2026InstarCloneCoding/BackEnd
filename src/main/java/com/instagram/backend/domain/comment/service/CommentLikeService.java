package com.instagram.backend.domain.comment.service;

import com.instagram.backend.domain.comment.entity.Comment;
import com.instagram.backend.domain.comment.entity.CommentLike;
import com.instagram.backend.domain.comment.repository.CommentLikeRepository;
import com.instagram.backend.domain.comment.repository.CommentRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private void validateIds(Long memberId, Long postId, Long commentId) {
        if (memberId == null || postId == null || commentId == null) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
    }

    // 댓글 좋아요
    @Transactional
    public void likeComment(Long memberId, Long postId, Long commentId) {
        // ① 값 자체 유효성 검증
        validateIds(memberId, postId, commentId);

        // ② DB 존재 여부 확인
        postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // ③ 비즈니스 규칙 검증
        if (!comment.getPostId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (commentLikeRepository.existsByMemberIdAndCommentId(memberId, commentId)) {
            throw new BusinessException(ErrorCode.ALREADY_COMMENT_LIKED);
        }

        try {
            commentLikeRepository.save(CommentLike.builder()
                    .memberId(memberId)
                    .commentId(commentId)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_COMMENT_LIKED);
        }

        // 원자적 UPDATE — 0건이면 댓글이 삭제됐거나 경합 발생 → 롤백
        int updated = commentRepository.incrementLikeCountAndIsDeletedFalse(commentId);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    // 댓글 좋아요 취소
    @Transactional
    public void unlikeComment(Long memberId, Long postId, Long commentId) {
        // ① 값 자체 유효성 검증
        validateIds(memberId, postId, commentId);

        // ② DB 존재 여부 확인
        postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // ③ 비즈니스 규칙 검증
        if (!comment.getPostId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        CommentLike commentLike = commentLikeRepository.findByMemberIdAndCommentId(memberId, commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_LIKE_NOT_FOUND));

        commentLikeRepository.delete(commentLike);

        // 원자적 UPDATE — 0건이면 댓글이 삭제됐거나 경합 발생 → 롤백
        int updated = commentRepository.decrementLikeCountAndIsDeletedFalse(commentId);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
    }
}
