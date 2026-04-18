package com.instagram.backend.domain.hashtag.entity;

import com.instagram.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hashtags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashtagId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private int postCount = 0;

    @Builder
    public Hashtag(String name) {
        this.name = name;
        this.postCount = 0;
    }

    public void increasePostCount() {
        this.postCount++;
    }

    public void decreasePostCount() {
        if (this.postCount > 0) this.postCount--;
    }
}
