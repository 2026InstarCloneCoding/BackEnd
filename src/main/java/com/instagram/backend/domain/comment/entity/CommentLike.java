package com.instagram.backend.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentLikeId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long commentId;

    @Builder
    public CommentLike(Long memberId, Long commentId) {
        this.memberId = memberId;
        this.commentId = commentId;
    }
}
