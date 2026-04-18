package com.instagram.backend.domain.hashtag.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hashtag_follows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HashtagFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashtagFollowId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long hashtagId;

    @Builder
    public HashtagFollow(Long memberId, Long hashtagId) {
        this.memberId = memberId;
        this.hashtagId = hashtagId;
    }
}
