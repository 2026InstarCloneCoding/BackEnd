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
    private Long memberId;

    @Column(name = "member_user_id", unique = true)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "member_password", nullable = false)
    private String memberPassword;

    @Column(name = "member_username", nullable = false, unique = true)
    private String memberUsername;

    @Column(name = "member_name", nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String role;

    @Column(length = 160)
    private String bio;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String imageType;

    @Column(length = 255)
    private String imageName;

    @Column(length = 36)
    private String imageUuid;

    @Column(nullable = false)
    private Long followerCount = 0L;

    @Column(nullable = false)
    private Long followingCount = 0L;

    // DTO 호환 getter
    public String getUsername() { return this.memberUsername; }

    @Builder
    public Member(String userId, String email, String memberPassword, String memberUsername, String memberName) {
        this.userId = userId;
        this.email = email;
        this.memberPassword = memberPassword;
        this.memberUsername = memberUsername;
        this.name = memberName;
        this.role = "ROLE_USER";
        this.followerCount = 0L;
        this.followingCount = 0L;
    }

    public void updateProfile(String name, String bio, String phone, Gender gender) {
        if (name != null) this.name = name;
        if (bio != null) this.bio = bio;
        if (phone != null) this.phone = phone;
        if (gender != null) this.gender = gender;
    }

    public void updateProfileImage(String imageUrl, String imageType, String imageName, String imageUuid) {
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.imageName = imageName;
        this.imageUuid = imageUuid;
    }

    /*
      팔로워/팔로잉 카운트 증감 메서드
      — 팔로우/언팔로우 시 실시간으로 카운트를 변경
      — 매번 COUNT 쿼리를 날리지 않고 숫자만 +1/-1 하므로 성능이 좋음 (비정규화)
      — 0 미만으로 내려가지 않도록 방어 코드 추가
    */
    public void incrementFollowerCount() { this.followerCount++; }
    public void decrementFollowerCount() { if (this.followerCount > 0) this.followerCount--; }
    public void incrementFollowingCount() { this.followingCount++; }
    public void decrementFollowingCount() { if (this.followingCount > 0) this.followingCount--; }
}
