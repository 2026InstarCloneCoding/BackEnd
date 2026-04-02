package com.instagram.backend.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageImageId;

    @Column(nullable = false)
    private Long messageId;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String imageType;

    @Column(length = 255)
    private String imageName;

    @Column(length = 36)
    private String imageUuid;

    @Builder
    public MessageImage(Long messageId, String imageUrl, String imageType, String imageName, String imageUuid) {
        this.messageId = messageId;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.imageName = imageName;
        this.imageUuid = imageUuid;
    }
}
