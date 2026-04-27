package com.instagram.backend.domain.post.repository;

import com.instagram.backend.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findByPostPostIdOrderBySortOrderAsc(Long postId);

    // 여러 게시물의 대표 이미지(sort_order=0)를 한 번의 쿼리로 조회
    // SELECT * FROM post_images WHERE post_id IN (?, ?, ...) AND sort_order = ?
    // — 북마크 목록 같은 썸네일 그리드 화면에서 N+1 문제를 막기 위해 사용
    // — sort_order=0인 첫 번째 이미지가 인스타그램의 "대표 썸네일" 역할
    List<PostImage> findByPostPostIdInAndSortOrder(Collection<Long> postIds, int sortOrder);

    // 피드용 — 여러 게시물의 전체 이미지 일괄 조회 (N+1 방지)
    List<PostImage> findByPostPostIdInOrderBySortOrderAsc(Collection<Long> postIds);
}
