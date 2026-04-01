package com.instagram.backend.domain.post.entity;

import com.instagram.backend.global.entity.BaseSoftDeleteEntity;
import com.instagram.backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "post_contents", columnDefinition = "TEXT")//DB에서 자바의 String 타입을 TEXT로 인식 하도록
    private String contents;// 자바에서

    @Column(name = "comment_enabled",nullable = false)
    private boolean commentEnabled = true;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    // 게시물 수정
    public void update(String contents, boolean commentEnabled) {
        this.contents = contents;
        this.commentEnabled = commentEnabled;
    }

    @Builder
    public Post(Member member, String contents, boolean commentEnabled) {
        this.member = member;
        this.contents = contents;
        this.commentEnabled = commentEnabled;
    }
}
