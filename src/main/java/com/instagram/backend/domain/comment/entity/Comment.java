package com.instagram.backend.domain.comment.entity;

import com.instagram.backend.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long postId;

    private Long parentCommentId;

    @Column(name = "comment_content", nullable = false, length = 500)
    private String commentContent;

    @Column(nullable = false)
    private int likeCount = 0;

    @Builder
    public Comment(Long memberId, Long postId, Long parentCommentId, String commentContent) {
        this.memberId = memberId;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.commentContent = commentContent;
        this.likeCount = 0;
    }
}
