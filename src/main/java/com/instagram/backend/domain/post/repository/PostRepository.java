package com.instagram.backend.domain.post.repository;

import com.instagram.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;// Spring Data JPA 기능 SQL을 짜지 않아도 기본적인 CRUD 자동 생성
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    //Optional : Null 값 안전하게 처리
    //findByIdAndIsDeletedFalse : ID로 찾고 IsDeleted가 false 것만
    // Spring Data JPA는 메서드 이름만 규칙에 맞게 영어 문장처럼 지어주면, 알아서 DB가 이해할 수 있는 SQL 쿼리로 번역

    // SELECT COUNT(*) FROM posts WHERE member_id = ? AND is_deleted = false
    // → 특정 유저의 삭제되지 않은 게시물 수 (프로필의 "게시물 N" 표시용)
    long countByMemberMemberIdAndIsDeletedFalse(Long memberId);
}
