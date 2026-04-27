package com.instagram.backend.domain.post.repository;

import com.instagram.backend.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    // 피드용 — 내가 좋아요한 postId Set (O(1) 조회로 N+1 방지)
    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.memberId = :memberId AND pl.postId IN :postIds")
    Set<Long> findPostIdsByMemberIdAndPostIdIn(
            @Param("memberId") Long memberId,
            @Param("postIds") Collection<Long> postIds);
}
