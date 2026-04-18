package com.instagram.backend.domain.hashtag.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hashtag_follows", uniqueConstraints = @UniqueConstraint(name = "uk_hashtag_follows_member_hashtag", columnNames = {"member_id", "hashtag_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HashtagFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashtagFollowId;

    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @Column(nullable = false, name = "hashtag_id")
    private Long hashtagId;

    @Builder
    public HashtagFollow(Long memberId, Long hashtagId) {
        this.memberId = memberId;
        this.hashtagId = hashtagId;
    }
}
