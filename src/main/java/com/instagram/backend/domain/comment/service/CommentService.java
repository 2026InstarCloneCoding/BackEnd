package com.instagram.backend.domain.comment.service;

import com.instagram.backend.domain.comment.dto.request.CommentCreateRequest;
import com.instagram.backend.domain.comment.dto.response.CommentCreateResponse;
import com.instagram.backend.domain.comment.dto.response.CommentListResponse;
import com.instagram.backend.domain.comment.entity.Comment;
import com.instagram.backend.domain.comment.repository.CommentRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //댓글 조회
    public CommentListResponse getComments(Long postId, Long cursor, int size){
        //댓글 조회 횟수 제한 30개
        if (size < 1 || size > 30) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }

        // 게시물 있는 지 확인
        postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        //최상위 댓글 조회(처음 조회와 이후 조회)
        PageRequest pageable = PageRequest.of(0, size +1);
        List<Comment> rawComments = (cursor ==null) ? commentRepository.findByPostIdAndParentCommentIdIsNullAndIsDeletedFalseOrderByCommentIdAsc(postId, pageable) :
                commentRepository.findByPostIdAndParentCommentIdIsNullAndIsDeletedFalseAndCommentIdGreaterThanOrderByCommentIdAsc(postId,cursor,pageable);
        //뒤에 데이터 있으면 사이즈만큼 댓글 가져오고 가장 마지막 ID 값을 다음 커서로 두기
        boolean hasMore = rawComments.size() > size;
        List<Comment> comments = hasMore ? rawComments.subList(0, size) : rawComments;
        Long nextCursor = hasMore ? comments.get(comments.size() -1).getCommentId() : null;
        //댓글 없을 때
        if (comments.isEmpty()){
            return CommentListResponse.builder()
                    .comments(List.of())
                    .nextCursor(null)
                    .hasMore(false)
                    .build();
        }
        // 대댓글 일괄 조회
        List<Long> commentIds = comments.stream().map(Comment::getCommentId).toList();
        Map<Long, List<Comment>> repliesMap = commentRepository
                .findRepliesByParentCommentIds(commentIds)
                .stream()
                .collect(Collectors.groupingBy(Comment::getParentCommentId));

        // memberId 전체 수집 후 한 번에 조회 (N+1 방지)
        Set<Long> memberIds = Stream.concat(
                comments.stream().map(Comment::getMemberId), //ID 번호만 나열
                repliesMap.values().stream().flatMap(List::stream).map(Comment::getMemberId)
        ).collect(Collectors.toSet());// 작성자 번호만 챙겨서 유저중복 방지
        //메모리에서 즉시 유저 정보를 꺼내 쓰기 위한 코드
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds)
                .stream()//멤버 id를 스트림
                .collect(Collectors.toMap(Member::getMemberId, m -> m));//map 형태로 나와야 하니까

        // 응답 조립
        List<CommentListResponse.CommentInfo> commentInfos = comments.stream()
                .map(comment -> {
                    List<CommentListResponse.ReplyInfo> replies = repliesMap //댓글의 대댓글을 조회
                            .getOrDefault(comment.getCommentId(), List.of())//있으면 대댓글 조회 없으면 빈값
                            .stream()
                            .map(reply -> new CommentListResponse.ReplyInfo(//DTO에 맞게 변환
                                    reply, memberMap.get(reply.getMemberId())))
                            .toList();
                    return new CommentListResponse.CommentInfo(comment, memberMap.get(comment.getMemberId()), replies);
                })
                .toList();

        return CommentListResponse.builder()
                .comments(commentInfos)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }
}
