package com.instagram.backend.domain.post.repository;

import com.instagram.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;// Spring Data JPA 기능 SQL을 짜지 않아도 기본적인 CRUD 자동 생성

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {//<다룰 앤티티,PK 타입> => save(), findAll(), delete() 바로 가져다 쓸 수 있
    Optional<Post> findByPostIdAndIsDeletedFalse(Long postId);
    //Optional : Null 값 안전하게 처리
    //findByIdAndIsDeletedFalse : ID로 찾고 IsDeleted가 false 것만
    // Spring Data JPA는 메서드 이름만 규칙에 맞게 영어 문장처럼 지어주면, 알아서 DB가 이해할 수 있는 SQL 쿼리로 번역
}
