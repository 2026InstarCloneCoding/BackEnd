package com.instagram.backend.domain.hashtag.repository;

import com.instagram.backend.domain.hashtag.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);
    // 이름 목록으로 일괄 조회 (N+1 방지)
    List<Hashtag> findByNameIn(List<String> names);
    // ID 목록으로 일괄 조회
    List<Hashtag> findByHashtagIdIn(List<Long> hashtagIds);

}
