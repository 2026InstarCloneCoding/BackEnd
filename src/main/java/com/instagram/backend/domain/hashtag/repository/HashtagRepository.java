package com.instagram.backend.domain.hashtag.repository;

import com.instagram.backend.domain.hashtag.entity.Hashtag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);
    // 이름 목록으로 일괄 조회 (N+1 방지)
    List<Hashtag> findByNameIn(List<String> names);
    // ID 목록으로 일괄 조회
    List<Hashtag> findByHashtagIdIn(List<Long> hashtagIds);

    /*
      해시태그 LIKE 검색 — GET /api/search/hashtags

      정책:
        — name LIKE '%q%' 로 부분 일치 검색
        — 정렬: postCount DESC (인기순) → name ASC (보조)
        — ESCAPE '\\' 절: 서비스 레이어에서 '%', '_', '\' 를 이스케이프한 검색어를 받아
          LIKE 와일드카드로 해석되는 것을 차단 (LIKE 패턴 인젝션 방어)

      Pageable:
        — limit 만 사용 (offset 0 고정). 추후 커서 페이징 도입 시 메서드 추가
    */
    @Query("SELECT h FROM Hashtag h " +
            "WHERE h.name LIKE CONCAT('%', :q, '%') ESCAPE '\\' " +
            "ORDER BY h.postCount DESC, h.name ASC")
    List<Hashtag> searchByName(@Param("q") String q, Pageable pageable);

}
