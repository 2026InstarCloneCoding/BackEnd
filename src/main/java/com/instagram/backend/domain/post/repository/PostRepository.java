package com.instagram.backend.domain.post.repository;

import com.instagram.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;// Spring Data JPA 기능 SQL을 짜지 않아도 기본적인 CRUD 자동 생성
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {//<다룰 앤티티,PK 타입> => save(), findAll(), delete() 바로 가져다 쓸 수 있
    Optional<Post> findByPostIdAndIsDeletedFalse(Long postId);

    // member를 JOIN FETCH로 함께 조회하여 N+1 방지
    @Query("SELECT p FROM Post p JOIN FETCH p.member WHERE p.postId = :postId AND p.isDeleted = false")
    Optional<Post> findByPostIdWithMemberAndIsDeletedFalse(@Param("postId") Long postId);

    // SELECT * FROM posts WHERE post_id IN (?, ?, ...) AND is_deleted = false
    // — 여러 post_id를 한 번의 쿼리로 조회 (N+1 문제 회피)
    // — 북마크 목록 조회처럼 "관련 post들을 한 번에 가져와야 하는" 상황에 사용
    List<Post> findByPostIdInAndIsDeletedFalse(Collection<Long> postIds);

    // → 특정 유저의 삭제되지 않은 게시물 수 (프로필의 "게시물 N" 표시용)
    long countByMemberMemberIdAndIsDeletedFalse(Long memberId);

    // 피드 첫 페이지 (cursor 없음) — JOIN FETCH로 N+1 방지
    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN FETCH p.member
        WHERE p.isDeleted = false
          AND (p.member.memberId IN :followingMemberIds OR p.postId IN :hashtagPostIds)
        ORDER BY p.postId DESC
    """)
    List<Post> findFeedPostsFirst(
            @Param("followingMemberIds") List<Long> followingMemberIds,
            @Param("hashtagPostIds") List<Long> hashtagPostIds,
            Pageable pageable);

    // 피드 다음 페이지 (cursor 있음)
    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN FETCH p.member
        WHERE p.isDeleted = false
          AND (p.member.memberId IN :followingMemberIds OR p.postId IN :hashtagPostIds)
          AND p.postId < :cursor
        ORDER BY p.postId DESC
    """)
    List<Post> findFeedPostsWithCursor(
            @Param("followingMemberIds") List<Long> followingMemberIds,
            @Param("hashtagPostIds") List<Long> hashtagPostIds,
            @Param("cursor") Long cursor,
            Pageable pageable);
}
