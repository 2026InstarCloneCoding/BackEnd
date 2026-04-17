package com.instagram.backend.domain.post.repository;

import com.instagram.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;// Spring Data JPA 기능 SQL을 짜지 않아도 기본적인 CRUD 자동 생성

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {//<다룰 앤티티,PK 타입> => save(), findAll(), delete() 바로 가져다 쓸 수 있
    Optional<Post> findByPostIdAndIsDeletedFalse(Long postId);

    // — 여러 post_id를 한 번의 쿼리로 조회 (N+1 문제 회피)
    // — 북마크 목록 조회처럼 "관련 post들을 한 번에 가져와야 하는" 상황에 사용
    List<Post> findByPostIdInAndIsDeletedFalse(Collection<Long> postIds);

    // → 특정 유저의 삭제되지 않은 게시물 수 (프로필의 "게시물 N" 표시용)
    long countByMemberMemberIdAndIsDeletedFalse(Long memberId);
}
