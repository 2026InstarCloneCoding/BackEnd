package com.instagram.backend.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "post_likes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_post_likes_post_member",
        columnNames = {"post_id", "member_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postLikeId;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long memberId;

    @Builder
    public PostLike(Long postId, Long memberId) {
        this.postId = postId;
        this.memberId = memberId;
    }
}
