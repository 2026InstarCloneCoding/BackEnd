package com.instagram.backend.domain.story.entity;

import com.instagram.backend.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Story extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storyId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String imageType;

    @Column(length = 255)
    private String imageName;

    @Column(length = 36)
    private String imageUuid;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public Story(Long memberId, String imageUrl, String imageType, String imageName, String imageUuid, LocalDateTime expiresAt) {
        this.memberId = memberId;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.imageName = imageName;
        this.imageUuid = imageUuid;
        this.expiresAt = expiresAt;
    }
}
