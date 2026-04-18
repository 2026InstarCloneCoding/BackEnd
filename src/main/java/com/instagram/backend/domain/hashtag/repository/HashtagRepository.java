package com.instagram.backend.domain.hashtag.repository;

import com.instagram.backend.domain.hashtag.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);
    // in절로 일괄조회
    List<Hashtag> findByHashtagIdIn(List<Long> hashtagIds);

}
