package com.instagram.backend.domain.member.entity;

import com.instagram.backend.domain.member.enums.Gender;
import com.instagram.backend.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "member_password", nullable = false)
    private String memberPassword;

    @Column(name = "member_username", nullable = false, unique = true)
    private String memberUsername;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "member_bio", length = 500)
    private String bio;

    @Column(name = "member_phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_gender", length = 10)
    private Gender gender;

    @Column(name = "member_image_url", length = 500)
    private String imageUrl;

    @Column(name = "member_image_type", length = 50)
    private String imageType;

    @Column(name = "member_image_name", length = 255)
    private String imageName;

    @Column(name = "member_image_uuid", length = 36)
    private String imageUuid;

    @Column(nullable = false)
    private Long followerCount = 0L;

    @Column(nullable = false)
    private Long followingCount = 0L;

    @Column(nullable = false)
    private String role;

    @Builder
    public Member(String userId, String email, String memberPassword, String memberUsername, String memberName) {
        this.userId = userId;
        this.email = email;
        this.memberPassword = memberPassword;
        this.memberUsername = memberUsername;
        this.memberName = memberName;
        this.role = "ROLE_USER";
    }
}
