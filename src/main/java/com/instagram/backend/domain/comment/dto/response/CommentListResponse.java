package com.instagram.backend.domain.comment.dto.response;

import com.instagram.backend.domain.comment.entity.Comment;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentListResponse {

    private List<CommentInfo> comments;
    private Long nextCursor;
    private boolean hasMore;

    @Getter
    public static class CommentInfo{
        private final Long commentId;
        private final String commentContent;
        private final int likeCount;
        private final Long parentCommentId;
        private final LocalDateTime createdAt;
        private final MemberInfo member;
        private final List<ReplyInfo> replies;

        public CommentInfo(Comment comment, Member member, List<ReplyInfo> replies){
            this.commentId = comment.getCommentId();
            this.commentContent = comment.getCommentContent();
            this.likeCount = comment.getLikeCount();
            this.parentCommentId = comment.getParentCommentId();
            this.createdAt = comment.getCreatedAt();
            this.member = MemberInfo.from(member);
            this.replies = replies;
        }
    }

    @Getter
    public static class ReplyInfo{
        private final Long commentId;
        private final String commentContent;
        private final int likeCount;
        private final Long parentCommentId;
        private final LocalDateTime createdAt;
        private final MemberInfo member;

        public ReplyInfo(Comment comment, Member member){
            this.commentId = comment.getCommentId();
            this.commentContent = comment.getCommentContent();
            this.likeCount = comment.getLikeCount();
            this.parentCommentId = comment.getParentCommentId();
            this.createdAt = comment.getCreatedAt();
            this.member = MemberInfo.from(member);
        }
        }
        
    @Getter
    public static class MemberInfo{
        private final Long memberId;
        private final String username;
        private final String imageUrl;

        private MemberInfo(Long memberId, String username, String imageUrl){
            this.memberId = memberId;
            this.username = username;
            this.imageUrl = imageUrl;
        }
        public static MemberInfo from(Member member){
            return new MemberInfo(
                    member.getMemberId(),
                    member.getMemberUsername(),
                    member.getImageUrl()
            );
        }
    }
}
