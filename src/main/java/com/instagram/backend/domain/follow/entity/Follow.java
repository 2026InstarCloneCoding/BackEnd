package com.instagram.backend.domain.follow.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
  Follow 엔티티 — follows 테이블 매핑
  팔로우 관계를 저장: "followerId가 followingId를 팔로우한다"

  여기서는 Member 엔티티를 직접 참조(@ManyToOne)하지 않고 ID만 저장
  → 단순한 관계 테이블에서는 이렇게 하면 불필요한 JOIN을 피할 수 있음
  → 필요할 때만 MemberRepository로 조회하면 됨
*/
@Entity
@Table(name = "follows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @Column(nullable = false)
    private Long followerId;

    @Column(nullable = false)
    private Long followingId;

    @Builder
    public Follow(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }
}
