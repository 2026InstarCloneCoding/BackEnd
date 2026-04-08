package com.instagram.backend.domain.bookmark.repository;

import com.instagram.backend.domain.bookmark.entity.Bookmark;
import com.instagram.backend.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByPostIdAndMemberId(Long postId, Long memberId);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
}
