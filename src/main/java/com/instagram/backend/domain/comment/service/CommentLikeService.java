package com.instagram.backend.domain.comment.service;

import com.instagram.backend.domain.comment.entity.Comment;
import com.instagram.backend.domain.comment.entity.CommentLike;
import com.instagram.backend.domain.comment.repository.CommentLikeRepository;
import com.instagram.backend.domain.comment.repository.CommentRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    //댓글 좋아요
    @Transactional
    public void likeComment(Long memberId, Long postId, Long commentId) {
        //게시물 확인
        postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        //댓글 확인
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        //해당 게시물 댓글인지 확인
        if(!comment.getPostId().equals(postId)){
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        //좋아요가 있는 지 확인
        if (commentLikeRepository.existsByMemberIdAndCommentId(memberId,commentId)){
            throw new BusinessException(ErrorCode.ALREADY_COMMENT_LIKED);
        }

        commentLikeRepository.save(CommentLike.builder()
                .memberId(memberId)
                .commentId(commentId)
                .build());
        // 비정규 카운트 up
        comment.increaseLikeCount();
    }
    //댓글 좋아요 취소
    public void unlikeComment(Long memberId, Long postId, Long commentId){
        //게시물 확인
        postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        //댓글 확인
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        // 좋아요 삭제할 부분 찾기(좋아요 이미 했는가 안했는가)
        CommentLike commentLike = commentLikeRepository.findByMemberIdAndCommentId(memberId,commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_LIKE_NOT_FOUND));

        commentLikeRepository.delete(commentLike);
        //비정규 좋아요 down
        comment.decreaseLikeCount();
    }
}
