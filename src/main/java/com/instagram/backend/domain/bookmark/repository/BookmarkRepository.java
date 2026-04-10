package com.instagram.backend.domain.bookmark.repository;

import com.instagram.backend.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByPostIdAndMemberId(Long postId, Long memberId);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    // 저장한 게시물 목록 조회 (커서 페이지네이션 — 첫 페이지)
    // SELECT * FROM bookmarks WHERE member_id = ? ORDER BY bookmark_id DESC LIMIT ?
    // — bookmark_id는 AUTO_INCREMENT이므로 큰 ID일수록 최근에 저장한 것
    // — 별도 created_at 컬럼 없이 PK 정렬만으로 "최근 저장순"을 구현 가능
    List<Bookmark> findByMemberIdOrderByBookmarkIdDesc(Long memberId, Pageable pageable);

    // 저장한 게시물 목록 조회 (커서 페이지네이션 — 두 번째 페이지부터)
    // 이전 페이지의 마지막 bookmark_id보다 작은 것들만 조회
    // — LessThan 조건으로 "이미 본 데이터 이후" 만 가져오는 게 커서 방식의 핵심
    List<Bookmark> findByMemberIdAndBookmarkIdLessThanOrderByBookmarkIdDesc(
            Long memberId, Long cursor, Pageable pageable);
}
