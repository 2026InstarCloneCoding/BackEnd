package com.instagram.backend.domain.follow.repository;

import com.instagram.backend.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

/*
  FollowRepository — follows 테이블에 접근하는 Repository

  countBy~ 메서드 — Spring Data JPA가 자동으로 COUNT 쿼리 생성
  existsBy~ 메서드 — 존재 여부를 boolean으로 반환 (COUNT보다 성능 좋음)
*/
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // SELECT COUNT(*) FROM follows WHERE following_id = ?
    // → 특정 유저를 팔로우하는 사람 수 (팔로워 수)
    long countByFollowingId(Long followingId);

    // SELECT COUNT(*) FROM follows WHERE follower_id = ?
    // → 특정 유저가 팔로우하는 사람 수 (팔로잉 수)
    long countByFollowerId(Long followerId);

    // SELECT EXISTS(SELECT 1 FROM follows WHERE follower_id = ? AND following_id = ?)
    // → 내가(followerId) 상대(followingId)를 팔로우하고 있는지 확인
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
