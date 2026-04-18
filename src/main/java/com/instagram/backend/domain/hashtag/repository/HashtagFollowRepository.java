package com.instagram.backend.domain.hashtag.repository;

import com.instagram.backend.domain.hashtag.entity.HashtagFollow;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HashtagFollowRepository extends JpaRepository<HashtagFollow, Long> {
    // 중복방지
    boolean existsByMemberIdAndHashtagId(Long memberId , Long hashtagId);
    //언팔 시 사용
    Optional<HashtagFollow> findByMemberIdAndHashtagId(Long memberId, Long hashtagId);
    // 내가 팔로우하는 해시태그 ID 목록 (피드 확장용)
    @Query("SELECT hf.hashtagId FROM HashtagFollow hf WHERE hf.memberId = :memberId")
    List<Long> findHashtagIdsByMemberId(@Param("memberId") Long memberId);
}
