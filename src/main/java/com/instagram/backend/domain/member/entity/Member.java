package com.instagram.backend.domain.member.entity;

import com.instagram.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "member_password", nullable = false)
    private String memberPassword;

    @Column(name = "member_username", nullable = false, unique = true)
    private String memberUsername;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    // 팀원이 필드를 자유롭게 추가하세요 (bio, memberImageUrl 등)

    @Column(nullable = false)
    private String role;

    @Builder
    public Member(String email, String memberPassword, String memberUsername, String memberName) {
        this.email = email;
        this.memberPassword = memberPassword;
        this.memberUsername = memberUsername;
        this.memberName = memberName;
        this.role = "ROLE_USER";
    }
}
