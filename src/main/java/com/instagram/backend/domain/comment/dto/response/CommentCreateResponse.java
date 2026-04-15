package com.instagram.backend.domain.comment.dto.response;

import com.instagram.backend.domain.comment.entity.Comment;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentCreateResponse {

    private final Long commentId;
    private final String commentContent;
    private final int likeCount;
    private final Long parentCommentId;
    private final LocalDateTime createdAt;
    private final MemberInfo member;

    private CommentCreateResponse(Comment comment, Member member) {
        this.commentId = comment.getCommentId();
        this.commentContent = comment.getCommentContent();
        this.likeCount = comment.getLikeCount();
        this.parentCommentId = comment.getParentCommentId();
        this.createdAt = comment.getCreatedAt();
        this.member = MemberInfo.from(member);
    }

    public static CommentCreateResponse of(Comment comment, Member member) {
        return new CommentCreateResponse(comment, member);
    }

    @Getter
    public static class MemberInfo {
        private final Long memberId;
        private final String username;
        private final String imageUrl;

        private MemberInfo(Long memberId, String username, String imageUrl) {
            this.memberId = memberId;
            this.username = username;
            this.imageUrl = imageUrl;
        }

        public static MemberInfo from(Member member) {
            return new MemberInfo(
                    member.getMemberId(),
                    member.getMemberUsername(),
                    member.getImageUrl()
            );
        }
    }
}

