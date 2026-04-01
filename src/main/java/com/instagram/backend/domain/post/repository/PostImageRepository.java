package com.instagram.backend.domain.post.repository;

import com.instagram.backend.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findByPostPostIdOrderBySortOrderAsc(Long postId);

}
