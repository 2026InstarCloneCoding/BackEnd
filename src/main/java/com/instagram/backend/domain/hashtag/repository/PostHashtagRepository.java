package com.instagram.backend.domain.hashtag.repository;

import com.instagram.backend.domain.hashtag.entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {
    //게시물 삭제할 때 카운트 관련
    @Query("""
    SELECT ph.hashtagId FROM PostHashtag ph WHERE ph.postId = :postId
""")
    List<Long> findHashtagIdsByPostId(@Param("postId") Long postId);
    // 게시물의 여러 해시태그 일괄조회
    @Query("""
    SELECT DISTINCT ph.postId FROM PostHashtag ph WHERE ph.hashtagId IN :hashtagIds
""")
    List<Long> findPostIdsByHashtagIdIn(@Param("hashtagIds") List<Long> hashtagIds);

    //게시물 삭제할 때 모든 해시태그 삭제할 때
    List<PostHashtag> findByPostId(Long postId);
}
