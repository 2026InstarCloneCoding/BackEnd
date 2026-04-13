package com.instagram.backend.domain.comment.service;

import com.instagram.backend.domain.comment.dto.request.CommentCreateRequest;
import com.instagram.backend.domain.comment.dto.response.CommentCreateResponse;
import com.instagram.backend.domain.comment.entity.Comment;
import com.instagram.backend.domain.comment.repository.CommentRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    //댓글 생성
    @Transactional
    public CommentCreateResponse createComment(Long memberId, Long postId, CommentCreateRequest request){
        //빈값 안됨
        if (request.getCommentContent() == null || request.getCommentContent().isBlank()){
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        //글자 제한 500자
        if (request.getCommentContent().length() > 500){
            throw new BusinessException(ErrorCode.EXCEED_COMMENT_LENGTH);
        }
        //게시물 확인 + 댓글 허용 여부
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (!post.isCommentEnabled()) {
            throw new BusinessException(ErrorCode.COMMENT_DISABLED);
        }
        //대댓글이면 부모댓글이 있는가
        if (request.getParentCommentId() != null){
            commentRepository.findByCommentIdAndIsDeletedFalse(request.getParentCommentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Comment comment = Comment.builder()
                .memberId(memberId)
                .postId(postId)
                .parentCommentId(request.getParentCommentId())
                .commentContent(request.getCommentContent())
                .build();
        commentRepository.save(comment);
        return CommentCreateResponse.of(comment,member);
    }
    //댓글 삭제
    @Transactional
    public void deleteComment(Long memberId, Long postId, Long commentId){
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(()->new BusinessException(ErrorCode.POST_NOT_FOUND));
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        boolean isCommentAuthor = comment.getMemberId().equals(memberId);
        boolean isPostOwner = post.getMember().getMemberId().equals(memberId);
        if (!isCommentAuthor && !isPostOwner){
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }
        comment.softDelete();
    }
}
