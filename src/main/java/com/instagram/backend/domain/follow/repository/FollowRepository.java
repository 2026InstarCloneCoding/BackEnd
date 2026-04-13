package com.instagram.backend.domain.follow.repository;

import com.instagram.backend.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

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

    // 팔로우 관계를 조회 — 언팔로우 시 삭제할 Follow 엔티티를 찾기 위해 사용
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /*
      커서 기반 팔로워 목록 조회
      — followingId로 "이 유저를 팔로우하는 사람들" 조회
      — followIdLessThan: 이전 페이지의 마지막 follow_id보다 작은 것만 (커서)
      — OrderByFollowIdDesc: 최신 팔로우부터 보여줌

      Pageable 파라미터로 limit(몇 개 가져올지)를 제어
        — Spring Data JPA 메서드 이름에 limit를 직접 넣을 수 없음
        — 대신 Pageable(페이지 번호 + 사이즈)을 넘겨서 DB에서 LIMIT 처리
        — 호출 시: PageRequest.of(0, limit) → 첫 페이지에서 limit개만 가져옴

      Spring Data JPA 메서드 이름 규칙:
        findBy + 조건 + OrderBy + 정렬기준 + Desc
        → SELECT * FROM follows WHERE following_id = ? AND follow_id < ? ORDER BY follow_id DESC LIMIT ?
    */
    List<Follow> findByFollowingIdAndFollowIdLessThanOrderByFollowIdDesc(
            Long followingId, Long followIdCursor, Pageable pageable);

    // 첫 페이지용 (커서 없을 때) — 가장 최신 팔로워부터
    List<Follow> findByFollowingIdOrderByFollowIdDesc(Long followingId, Pageable pageable);

    /*
      커서 기반 팔로잉 목록 조회
      — followerId로 "이 유저가 팔로우하는 사람들" 조회
    */
    List<Follow> findByFollowerIdAndFollowIdLessThanOrderByFollowIdDesc(
            Long followerId, Long followIdCursor, Pageable pageable);

    // 첫 페이지용 (커서 없을 때)
    List<Follow> findByFollowerIdOrderByFollowIdDesc(Long followerId, Pageable pageable);

    // 내가 팔로우하는 사람들의 memberId 목록 조회 (스토리 피드용)
    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :followerId")
    List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);
}
