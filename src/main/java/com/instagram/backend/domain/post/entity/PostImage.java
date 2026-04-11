package com.instagram.backend.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long postImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_type", length = 50)
    private String imageType;

    @Column(name = "image_name", length = 255)
    private String imageName;

    @Column(name = "image_uuid", length = 36)
    private String imageUuid;

    @Column(name = "alt_text", length = 500)
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    public PostImage(Post post, String imageUrl, String imageType,
                     String imageName, String imageUuid, String altText, int sortOrder) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.imageName = imageName;
        this.imageUuid = imageUuid;
        this.altText = altText;
        this.sortOrder = sortOrder;
    }
}
